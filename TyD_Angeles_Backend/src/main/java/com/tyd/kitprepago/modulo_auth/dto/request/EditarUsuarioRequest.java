package com.tyd.kitprepago.modulo_auth.dto.request;

import com.tyd.kitprepago.shared.security.Rol;
import jakarta.validation.constraints.*;

public record EditarUsuarioRequest(
        @Size(min = 4, max = 100) String username,
        String nombreCompleto,
        @Email String email,
        @Size(min = 8) String password,    // null = no cambiar
        Rol rol,                            // null = no cambiar
        Long zonaId,
        Long sucursalId,
        Boolean activo
) {}
