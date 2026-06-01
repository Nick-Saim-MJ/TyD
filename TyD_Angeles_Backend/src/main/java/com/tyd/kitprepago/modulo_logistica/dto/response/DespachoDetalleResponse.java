package com.tyd.kitprepago.modulo_logistica.dto.response;

import com.tyd.kitprepago.modulo_logistica.entity.EstadoDespacho;
import java.time.Instant;
import java.util.List;

/** Respuesta de detalle — incluye cada item con su estado individual */
public record DespachoDetalleResponse(
    Long id,
    Long sucursalOrigenId,   String sucursalOrigenNombre,   String zonaOrigenNombre,
    Long sucursalDestinoId,  String sucursalDestinoNombre,  String zonaDestinoNombre,
    EstadoDespacho estado,
    String usuarioEnviaNombre,
    String usuarioRecibeNombre,
    String guiaRemision,
    String observaciones,
    Instant fechaDespacho,
    Instant fechaRecepcion,
    Instant createdAt,
    Boolean esInterZona,
    List<DespachoItemResponse> items,
    // Contadores para el resumen visual del despacho
    long totalEnviados,
    long totalRecibidosOk,
    long totalDefectuosos,
    long totalNoRecibidos
) {}
