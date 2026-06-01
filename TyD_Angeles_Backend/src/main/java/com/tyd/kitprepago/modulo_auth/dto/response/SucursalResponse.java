package com.tyd.kitprepago.modulo_auth.dto.response;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;

public record SucursalResponse(
    Long id,
    String nombre,
    Sucursal.TipoSucursal tipo,
    Long zonaId,
    String zonaNombre,
    String ubigeo,
    String direccion,
    Long ubicacionFisicaId,    // null si no comparte ubicación
    String ubicacionFisicaNombre,
    Boolean activo
) {}
