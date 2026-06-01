package com.tyd.kitprepago.modulo_auth.dto.response;

import com.tyd.kitprepago.shared.security.Rol;
import java.time.Instant;
import java.util.List;

/**
 * Perfil completo del usuario autenticado para GET /api/auth/me.
 * Angular lo usa para construir el sidebar y ocultar/mostrar opciones.
 * El campo "permisos" es una lista de strings que el frontend lee directamente.
 */
public record MeResponse(
        Long id,
        String username,
        String nombreCompleto,
        String email,
        Rol rol,
        Long zonaId,
        String zonaNombre,
        String zonaCodigoDirecTV,  // Z122000 etc.
        Long sucursalId,
        String sucursalNombre,
        Instant ultimoLogin,
        List<String> permisos      // ["VER_VENTAS", "CREAR_VENTA", "VER_REPORTES", ...]
) {}