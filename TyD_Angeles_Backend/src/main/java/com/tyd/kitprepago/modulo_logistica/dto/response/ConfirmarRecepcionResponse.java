package com.tyd.kitprepago.modulo_logistica.dto.response;

import com.tyd.kitprepago.modulo_logistica.entity.EstadoDespacho;

/** Respuesta resumida después de confirmar la recepción */
public record ConfirmarRecepcionResponse(
    Long despachoId,
    EstadoDespacho estadoFinal,
    long kitsRecibidosOk,
    long kitsDefectuosos,
    long kitsNoRecibidos,
    String mensaje
) {}
