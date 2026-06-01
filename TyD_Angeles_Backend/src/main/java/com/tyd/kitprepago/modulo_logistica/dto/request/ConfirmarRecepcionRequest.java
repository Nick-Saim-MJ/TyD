package com.tyd.kitprepago.modulo_logistica.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Payload de confirmación de recepción — kit por kit.
 * El receptor debe confirmar CADA item del despacho.
 * Items no incluidos en la lista se consideran NO_RECIBIDO automáticamente.
 */
public record ConfirmarRecepcionRequest(
    @NotEmpty @Valid List<ItemRecepcionRequest> items,
    /** Observaciones generales del despacho (además de las individuales por kit) */
    String observacionesGenerales
) {}
