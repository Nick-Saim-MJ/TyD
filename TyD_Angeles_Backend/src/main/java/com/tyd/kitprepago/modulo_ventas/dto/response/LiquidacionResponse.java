package com.tyd.kitprepago.modulo_ventas.dto.response;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
public record LiquidacionResponse(
    Long id,
    Long vendedorId, String vendedorNombre,
    Long sucursalId, String sucursalNombre, String zonaNombre,
    LocalDate periodoInicio, LocalDate periodoFin,
    BigDecimal montoTotalEsperado, BigDecimal montoDepositado, BigDecimal diferencia,
    EstadoLiquidacion estado,
    String aprobadoPorNombre,
    String observaciones,
    Instant fechaLiquidacion, Instant fechaAprobacion
) {}
