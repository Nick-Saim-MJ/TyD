package com.tyd.kitprepago.modulo_auth.dto.request;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import jakarta.validation.constraints.*;

public record CrearSucursalRequest(
        @NotBlank String nombre,
        @NotNull Sucursal.TipoSucursal tipo,
        @NotNull Long zonaId,
        @NotBlank @Size(min = 6, max = 10) String ubigeo,
        @NotBlank String direccion,
        Long ubicacionFisicaId    // null = sin vinculación física
) {}
