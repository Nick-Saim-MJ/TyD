package com.tyd.kitprepago.modulo_ventas.dto.request;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
public record CrearLiquidacionRequest(
    @NotNull Long vendedorId,
    @NotNull Long sucursalId,
    @NotNull LocalDate periodoInicio,
    @NotNull LocalDate periodoFin,
    String observaciones
) {}
