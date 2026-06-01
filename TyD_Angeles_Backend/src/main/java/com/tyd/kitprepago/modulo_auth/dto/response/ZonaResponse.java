package com.tyd.kitprepago.modulo_auth.dto.response;

public record ZonaResponse(
    Long id,
    String codigoZona,
    String nombre,
    String region,
    Boolean activo,
    Integer totalSucursales   // útil para la vista de admin
) {}
