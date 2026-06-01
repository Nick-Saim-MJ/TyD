package com.tyd.kitprepago.modulo_logistica.dto.response;

import com.tyd.kitprepago.modulo_logistica.entity.EstadoDespacho;
import java.time.Instant;

/** Respuesta de listado — sin el detalle de items para no sobrecargar */
public record DespachoResponse(
    Long id,
    Long sucursalOrigenId,   String sucursalOrigenNombre,
    Long zonaOrigenId,       String zonaOrigenNombre,
    Long sucursalDestinoId,  String sucursalDestinoNombre,
    Long zonaDestinoId,      String zonaDestinoNombre,
    EstadoDespacho estado,
    String usuarioEnviaNombre,
    String usuarioRecibeNombre,
    String guiaRemision,
    String observaciones,
    Integer totalItems,
    Instant fechaDespacho,
    Instant fechaRecepcion,
    Instant createdAt,
    Boolean esInterZona       // Flag útil para el frontend
) {}
