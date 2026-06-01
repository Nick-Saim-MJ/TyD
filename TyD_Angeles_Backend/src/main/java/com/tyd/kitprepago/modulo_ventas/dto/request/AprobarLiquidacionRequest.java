package com.tyd.kitprepago.modulo_ventas.dto.request;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record AprobarLiquidacionRequest(
    @NotNull @DecimalMin("0.00") BigDecimal montoDepositado,
    @NotNull EstadoLiquidacion nuevoEstado, // APROBADO | RECHAZADO | OBSERVADO
    String observaciones
) {}
