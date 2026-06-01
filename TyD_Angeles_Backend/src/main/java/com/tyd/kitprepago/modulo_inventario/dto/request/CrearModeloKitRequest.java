package com.tyd.kitprepago.modulo_inventario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearModeloKitRequest(
    @NotBlank @Size(max = 20) String codigo,
    @NotBlank @Size(max = 100) String nombre,
    @Size(max = 500) String descripcion,
    Boolean tieneDeco      // null = true por defecto
) {}
