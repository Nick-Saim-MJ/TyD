package com.tyd.kitprepago.shared.security;

import com.tyd.kitprepago.shared.exception.CuentaBloqueadaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Carga el usuario desde BD y lo convierte en UsuarioPrincipal.
 * Contiene la lógica de bloqueo por intentos fallidos.
 *
 * Depende de UsuarioSecurityRepository (interfaz) para no acoplar
 * el módulo shared con el módulo_auth directamente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioSecurityRepository usuarioRepo;

    static final int MAX_INTENTOS    = 5;
    static final int MINUTOS_BLOQUEO = 15;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioSecurityProjection u = usuarioRepo.findByUsernameForAuth(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        log.info("🔥 LOAD USER: {}", username);
        // Bloqueo activo: rechazar inmediatamente
        if (u.getBloqueadoHasta() != null && u.getBloqueadoHasta().isAfter(Instant.now())) {
            throw new CuentaBloqueadaException(u.getBloqueadoHasta());
        }

        return UsuarioPrincipal.builder()
                .id(u.getId())
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .nombreCompleto(u.getNombreCompleto())
                .rol(u.getRol())
                .zonaId(u.getZonaId())
                .sucursalId(u.getSucursalId())
                .activo(u.isActivo() && u.getDeletedAt() == null)
                .build();
    }

    /** Llamar cuando el login FALLA. Incrementa contador y bloquea si llega al límite. */
    @Transactional
    public void registrarIntentoFallido(String username) {
        usuarioRepo.findByUsernameForAuth(username).ifPresent(u -> {
            int intentos = u.getIntentosFallidos() + 1;
            if (intentos >= MAX_INTENTOS) {
                Instant hasta = Instant.now().plusSeconds(MINUTOS_BLOQUEO * 60L);
                usuarioRepo.bloquearCuenta(u.getId(), hasta, intentos);
                log.warn("Cuenta {} bloqueada {} min por {} intentos fallidos",
                        username, MINUTOS_BLOQUEO, intentos);
            } else {
                usuarioRepo.incrementarIntentosFallidos(u.getId(), intentos);
            }
        });
    }

    /** Llamar cuando el login es EXITOSO. Resetea intentos y actualiza ultimo_login. */
    @Transactional
    public void registrarLoginExitoso(String username) {
        usuarioRepo.findByUsernameForAuth(username)
                .ifPresent(u -> usuarioRepo.resetearBloqueo(u.getId(), Instant.now()));
    }
}
