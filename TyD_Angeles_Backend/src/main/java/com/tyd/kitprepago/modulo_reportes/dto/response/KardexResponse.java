package com.tyd.kitprepago.modulo_reportes.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record KardexResponse(
    Long id,
    Long sucursalId, String sucursalNombre,
    String zonaNombre,
    Long productoId, String productoNombre,
    String periodo,
    Integer stockInicio, Integer totalIngresos,
    Integer totalSalidas, Integer stockFin,
    BigDecimal totalLiquidado,
    Boolean cerrado,
    String generadoPorNombre,
    Instant createdAt, Instant updatedAt
) {}
