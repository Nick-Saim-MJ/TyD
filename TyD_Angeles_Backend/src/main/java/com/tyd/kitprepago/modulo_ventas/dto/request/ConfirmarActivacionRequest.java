package com.tyd.kitprepago.modulo_ventas.dto.request;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
public record ConfirmarActivacionRequest(
    @DecimalMin("0.00") BigDecimal montoRecargaInicial,
    String comentarios
) {}
