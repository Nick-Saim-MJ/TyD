package com.tyd.kitprepago.modulo_auth.service;

import com.tyd.kitprepago.modulo_auth.dto.request.LoginRequest;
import com.tyd.kitprepago.modulo_auth.dto.response.LoginResponse;
import com.tyd.kitprepago.modulo_auth.dto.response.MeResponse;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_auth.repository.UsuarioJpaRepository;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.security.JwtService;
import com.tyd.kitprepago.shared.security.Rol;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenBlacklistService blacklistService;
    private final UsuarioJpaRepository usuarioRepo;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // ─────────────────────────── LOGIN ───────────────────────────────────────

    /**
     * Flujo completo de autenticación:
     * 1. Spring Security verifica credenciales (llama a UserDetailsService internamente)
     * 2. Si falla → registrar intento fallido → relanzar excepción
     * 3. Si éxito → resetear bloqueo → generar JWT → construir LoginResponse
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // AuthenticationManager llama a loadUserByUsername → verifica bcrypt
            // Si bloqueado_hasta activo, loadUserByUsername lanza CuentaBloqueadaException
            // Si credenciales incorrectas, lanza BadCredentialsException
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException ex) {
            // Credenciales incorrectas: registrar intento y relanzar
            userDetailsService.registrarIntentoFallido(request.username());
            throw ex; // GlobalExceptionHandler lo convierte en 401
        }

        // Login exitoso
        UsuarioPrincipal principal = (UsuarioPrincipal)
                userDetailsService.loadUserByUsername(request.username());

        userDetailsService.registrarLoginExitoso(request.username());

        String token = jwtService.generateToken(principal);

        // Cargar zona/sucursal para la respuesta
        Usuario usuario = usuarioRepo.findByUsername(request.username())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", principal.getId()));

        log.info("Login exitoso: {} [{}]", request.username(), principal.getRol());

        return new LoginResponse(
                token,
                LoginResponse.BEARER,
                expirationMs,
                principal.getId(),
                principal.getUsername(),
                principal.getNombreCompleto(),
                principal.getRol(),
                principal.getZonaId(),
                usuario.getZona() != null ? usuario.getZona().getNombre() : null,
                principal.getSucursalId(),
                usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null
        );
    }

    // ─────────────────────────── LOGOUT ──────────────────────────────────────

    /**
     * Invalida el token en la blacklist en memoria.
     * @param bearerToken El header completo "Bearer <token>"
     */
    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) return;
        String token = bearerToken.substring(7);
        try {
            Instant expiracion = jwtService.extractClaim(token,
                    claims -> claims.getExpiration().toInstant());
            blacklistService.invalidar(token, expiracion);
            log.debug("Logout: token invalidado correctamente");
        } catch (Exception ex) {
            // Token ya inválido o expirado, no hace falta agregarlo a la blacklist
            log.debug("Logout: token ya inválido, ignorando. {}", ex.getMessage());
        }
    }

    // ─────────────────────────── ME ──────────────────────────────────────────

    /**
     * Devuelve el perfil completo del usuario autenticado.
     * Los permisos se calculan dinámicamente según el rol para que
     * Angular pueda construir el menú sin lógica de roles hardcodeada.
     */
    @Transactional(readOnly = true)
    public MeResponse me(UsuarioPrincipal principal) {
        Usuario usuario = usuarioRepo.findById(principal.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", principal.getId()));

        return new MeResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getZona() != null ? usuario.getZona().getId() : null,
                usuario.getZona() != null ? usuario.getZona().getNombre() : null,
                usuario.getZona() != null ? usuario.getZona().getCodigoZona() : null,
                usuario.getSucursal() != null ? usuario.getSucursal().getId() : null,
                usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null,
                usuario.getUltimoLogin(),
                calcularPermisos(usuario.getRol())
        );
    }

    // ─────────────────────────── HELPER: PERMISOS ────────────────────────────

    /**
     * Convierte el rol en una lista de strings que Angular usa para
     * mostrar/ocultar elementos del UI sin necesidad de conocer los roles.
     *
     * Todos los permisos son aditivos: ADMIN tiene todos.
     */
    private List<String> calcularPermisos(Rol rol) {
        List<String> permisos = new ArrayList<>();

        // Todos los roles autenticados
        permisos.add("VER_SUCURSALES");
        permisos.add("VER_ZONAS");

        switch (rol) {
            case ADMIN -> {
                permisos.addAll(List.of(
                        "CREAR_USUARIO", "EDITAR_USUARIO", "ELIMINAR_USUARIO",
                        "CREAR_SUCURSAL", "EDITAR_SUCURSAL",
                        "CREAR_ZONA", "EDITAR_ZONA",
                        "CREAR_LOTE", "VER_LOTES", "VER_TODOS_LOTES",
                        "VER_ITEMS_KIT", "CAMBIAR_ESTADO_KIT",
                        "CREAR_DESPACHO", "CONFIRMAR_RECEPCION", "DESPACHO_INTERZONA",
                        "CREAR_VENTA", "ANULAR_VENTA", "VER_TODAS_VENTAS",
                        "APROBAR_LIQUIDACION",
                        "VER_REPORTES", "GENERAR_KARDEX", "CERRAR_KARDEX",
                        "VER_AUDIT_LOG", "EXPORTAR_EXCEL"
                ));
            }
            case JEFE_ALMACEN -> {
                permisos.addAll(List.of(
                        "VER_LOTES", "VER_ITEMS_KIT", "CAMBIAR_ESTADO_KIT",
                        "CREAR_DESPACHO", "CONFIRMAR_RECEPCION", "DESPACHO_INTERZONA",
                        "CREAR_VENTA", "ANULAR_VENTA", "VER_TODAS_VENTAS",
                        "VER_REPORTES", "GENERAR_KARDEX", "CERRAR_KARDEX",
                        "EXPORTAR_EXCEL"
                ));
            }
            case ALMACENERO -> {
                permisos.addAll(List.of(
                        "VER_LOTES", "VER_ITEMS_KIT",
                        "CREAR_DESPACHO", "CONFIRMAR_RECEPCION",
                        "CREAR_VENTA", "VER_VENTAS_ZONA",
                        "EXPORTAR_EXCEL"
                ));
            }
            case VENDEDOR -> {
                permisos.addAll(List.of(
                        "VER_ITEMS_KIT",
                        "CREAR_VENTA", "VER_VENTAS_PROPIA"
                ));
            }
            case CONTADOR -> {
                permisos.addAll(List.of(
                        "VER_LOTES", "VER_TODAS_VENTAS",
                        "VER_REPORTES", "EXPORTAR_EXCEL"
                ));
            }
        }

        return permisos;
    }
}
