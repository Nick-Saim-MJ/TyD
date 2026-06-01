package com.tyd.kitprepago.modulo_auth.dto.request;

import com.tyd.kitprepago.shared.security.Rol;
import jakarta.validation.constraints.*;

public record CrearUsuarioRequest(
        @NotBlank @Size(min = 4, max = 100) String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String nombreCompleto,
        @Email String email,
        @NotNull Rol rol,
        Long zonaId,      // NULL para ADMIN/JEFE_ALMACEN
        Long sucursalId   // NULL para ADMIN/JEFE_ALMACEN/CONTADOR
) {}
