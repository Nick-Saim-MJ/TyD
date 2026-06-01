package com.tyd.kitprepago.modulo_auth.dto.request;

import jakarta.validation.constraints.*;

public record CrearZonaRequest(
        @NotBlank @Size(max = 20) String codigoZona,
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 100) String region
) {}
