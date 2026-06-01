package com.tyd.kitprepago.modulo_logistica.dto.request;

import com.tyd.kitprepago.modulo_logistica.entity.EstadoDespachoItem;
import jakarta.validation.constraints.NotNull;

public record ItemRecepcionRequest(
    /** ID del items_kit (no del despacho_item) — más natural para el frontend */
    @NotNull Long itemKitId,
    @NotNull EstadoDespachoItem estadoRecepcion,
    /** Obligatorio si estadoRecepcion = RECIBIDO_DEFECTUOSO */
    String observacion
) {}
