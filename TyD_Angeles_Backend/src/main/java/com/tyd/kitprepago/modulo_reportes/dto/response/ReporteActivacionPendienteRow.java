package com.tyd.kitprepago.modulo_reportes.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

/** Kit vendido sin activar — para GET /api/reportes/activaciones-pendientes */
public record ReporteActivacionPendienteRow(
    Long ventaId, Long activacionId,
    String zona, String sucursal,
    String serieSim, String serieMaestro, String producto,
    String clienteDni, String clienteNombre,
    String vendedor,
    BigDecimal montoVenta,
    BigDecimal montoRecargaInicial,
    Instant fechaVenta,
    long diasSinActivar        // NOW - fechaVenta en días
) {}
