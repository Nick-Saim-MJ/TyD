package com.tyd.kitprepago.modulo_inventario.dto.request;

import com.tyd.kitprepago.modulo_inventario.entity.EstadoKit;
import jakarta.validation.constraints.NotNull;

/**
 * Solo ADMIN y JEFE_ALMACEN pueden usarlo.
 * Estados permitidos vía este endpoint: DEFECTUOSO, DEVUELTO.
 * VENDIDO y TRANSITO se setean internamente por los flujos de venta/despacho.
 */
public record CambiarEstadoKitRequest(
    @NotNull EstadoKit nuevoEstado,
    @NotNull String motivo
) {}
