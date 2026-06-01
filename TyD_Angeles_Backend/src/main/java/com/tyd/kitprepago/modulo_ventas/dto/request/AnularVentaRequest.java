package com.tyd.kitprepago.modulo_ventas.dto.request;
import jakarta.validation.constraints.NotBlank;
public record AnularVentaRequest(@NotBlank String motivo) {}
