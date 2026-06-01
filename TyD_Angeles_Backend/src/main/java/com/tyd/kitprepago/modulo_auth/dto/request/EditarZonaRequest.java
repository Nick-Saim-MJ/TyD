package com.tyd.kitprepago.modulo_auth.dto.request;

import jakarta.validation.constraints.Size;

public record EditarZonaRequest(
        @Size(max = 20) String codigoZona,
        @Size(max = 100) String nombre,
        @Size(max = 100) String region,
        Boolean activo
) {}
