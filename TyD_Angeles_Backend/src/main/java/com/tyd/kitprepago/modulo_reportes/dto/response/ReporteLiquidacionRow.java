package com.tyd.kitprepago.modulo_reportes.dto.response;

import com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Fila del reporte de liquidaciones con diferencias */
public record ReporteLiquidacionRow(
    Long liquidacionId,
    String zona, String sucursal,
    String vendedor,
    LocalDate periodoInicio, LocalDate periodoFin,
    BigDecimal montoEsperado, BigDecimal montoDepositado, BigDecimal diferencia,
    EstadoLiquidacion estado,
    String aprobadoPor,
    Instant fechaLiquidacion, Instant fechaAprobacion
) {}
