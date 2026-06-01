package com.tyd.kitprepago.modulo_inventario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Un ítem dentro del payload de creación de lote.
 * serie_deco es opcional — el frontend lo omite si ModeloKit.tieneDeco=false.
 */
public record ItemKitIngresoRequest(
    @NotNull  Long productoId,
    Long modeloKitId,
    @NotBlank String serieMaestro,
    @NotBlank String serieSim,
    String serieDeco          // null para kits sin decodificador
) {}
