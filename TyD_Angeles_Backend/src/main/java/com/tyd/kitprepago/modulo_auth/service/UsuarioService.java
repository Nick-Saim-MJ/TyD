package com.tyd.kitprepago.modulo_auth.service;

import com.tyd.kitprepago.modulo_auth.dto.request.CrearUsuarioRequest;
import com.tyd.kitprepago.modulo_auth.dto.request.EditarUsuarioRequest;
import com.tyd.kitprepago.modulo_auth.dto.response.*;
import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_auth.entity.Zona;
import com.tyd.kitprepago.modulo_auth.mapper.AuthMapper;
import com.tyd.kitprepago.modulo_auth.repository.SucursalJpaRepository;
import com.tyd.kitprepago.modulo_auth.repository.UsuarioJpaRepository;
import com.tyd.kitprepago.shared.audit.Auditable;
import com.tyd.kitprepago.shared.audit.TipoAccion;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.security.Rol;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

// ═══════════════════════════════════════════════════════════════════════════
// USUARIO SERVICE
// ═══════════════════════════════════════════════════════════════════════════

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioJpaRepository usuarioRepo;
    private final SucursalJpaRepository sucursalRepo;
    private final ZonaContextHolder zonaCtx;
    private final AuthMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        // ADMIN ve todos, el resto no debería llamar este endpoint (SecurityConfig lo restringe)
        List<Usuario> usuarios = zonaCtx.getZonaIdFiltro()
                .map(usuarioRepo::findByZona_IdAndDeletedAtIsNull)
                .orElseGet(usuarioRepo::findByActivoTrueAndDeletedAtIsNull);
        return mapper.toUsuarioResponseList(usuarios);
    }

    /**
     * Autocomplete de vendedores/almaceneros para el formulario de venta.
     * Solo devuelve roles que pueden registrar ventas.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarVendedores(Long zonaIdParam, Long sucursalId) {
        // Si el frontend envía zonaId, lo usamos. Si no, caemos al contexto del JWT.
        Long zonaId = zonaIdParam != null
                ? zonaIdParam
                : zonaCtx.getZonaIdFiltro()
                  .orElseThrow(() -> new IllegalStateException("No se pudo determinar la zona del usuario"));

        zonaCtx.validarAccesoZona(zonaId);

        List<Usuario> vendedores;
        if (sucursalId != null) {
            vendedores = usuarioRepo.findByZona_IdAndSucursal_IdAndRolInAndActivoTrueAndDeletedAtIsNull(
                    zonaId, sucursalId, List.of(Rol.VENDEDOR, Rol.ALMACENERO, Rol.JEFE_ALMACEN)
            );
        } else {
            vendedores = usuarioRepo.findByZona_IdAndRolInAndActivoTrueAndDeletedAtIsNull(
                    zonaId, List.of(Rol.VENDEDOR, Rol.ALMACENERO, Rol.JEFE_ALMACEN)
            );
        }

        return mapper.toUsuarioResponseList(vendedores);
    }

    @Transactional
    @Auditable(tabla = "usuarios", accion = TipoAccion.INSERT)
    public UsuarioResponse crear(CrearUsuarioRequest req, UsuarioPrincipal admin) {
        if (usuarioRepo.existsByUsername(req.username())) {
            throw new IllegalArgumentException("El username '" + req.username() + "' ya existe");
        }

        Zona zona = req.zonaId() != null
                ? buscarZona(req.zonaId())
                : null;

        Sucursal sucursal = req.sucursalId() != null
                ? sucursalRepo.findById(req.sucursalId())
                  .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", req.sucursalId()))
                : null;

        Usuario creadoPor = usuarioRepo.findById(admin.getId()).orElse(null);

        Usuario nuevo = Usuario.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .nombreCompleto(req.nombreCompleto())
                .email(req.email())
                .rol(req.rol())
                .zona(zona)
                .sucursal(sucursal)
                .creadoPor(creadoPor)
                .build();

        return mapper.toUsuarioResponse(usuarioRepo.save(nuevo));
    }

    @Transactional
    @Auditable(tabla = "usuarios", accion = TipoAccion.UPDATE)
    public UsuarioResponse editar(Long id, EditarUsuarioRequest req, UsuarioPrincipal editor) {
        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        // Un usuario no puede cambiar su propio rol
        if (editor.getId().equals(id) && req.rol() != null && !req.rol().equals(usuario.getRol())) {
            throw new IllegalArgumentException("No puedes cambiar tu propio rol");
        }

        Optional.ofNullable(req.username()).ifPresent(usuario::setUsername);
        Optional.ofNullable(req.nombreCompleto()).ifPresent(usuario::setNombreCompleto);
        Optional.ofNullable(req.email()).ifPresent(usuario::setEmail);
        Optional.ofNullable(req.activo()).ifPresent(usuario::setActivo);
        Optional.ofNullable(req.rol()).ifPresent(usuario::setRol);

        if (req.password() != null && !req.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        if (req.zonaId() != null) {
            usuario.setZona(buscarZona(req.zonaId()));
        }
        if (req.sucursalId() != null) {
            usuario.setSucursal(sucursalRepo.findById(req.sucursalId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", req.sucursalId())));
        }

        return mapper.toUsuarioResponse(usuarioRepo.save(usuario));
    }

    @Transactional
    @Auditable(tabla = "usuarios", accion = TipoAccion.DELETE)
    public UsuarioResponse softDelete(Long id, UsuarioPrincipal admin) {
        if (admin.getId().equals(id)) {
            throw new IllegalArgumentException("No puedes eliminar tu propio usuario");
        }

        // 1. Buscamos el usuario mientras aún es "visible"
        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        // 2. Ejecutamos el borrado lógico
        usuario.setDeletedAt(Instant.now());
        usuario.setActivo(false);
        usuarioRepo.save(usuario); // Esto es mejor que un @Query manual para mantener consistencia

        // 3. Retornamos el mapeo del objeto que ya teníamos en memoria
        return mapper.toUsuarioResponse(usuario);
    }

    private Zona buscarZona(Long zonaId) {
        // Reusamos el JPA a través del repositorio de sucursal (tiene acceso a Zona)
        // En un módulo limpio, esto vendría del ZonaService
        throw new RecursoNoEncontradoException("Zona", zonaId); // placeholder
        // En implementación real: zonaRepo.findById(zonaId).orElseThrow(...)
    }
}
