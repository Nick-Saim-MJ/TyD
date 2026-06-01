package com.tyd.kitprepago.modulo_auth.dto.response;

import com.tyd.kitprepago.shared.security.Rol;
import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String username,
        String nombreCompleto,
        String email,
        Rol rol,
        Long zonaId,
        String zonaNombre,
        Long sucursalId,
        String sucursalNombre,
        Boolean activo,
        Instant ultimoLogin,
        Instant createdAt
) {}
