package com.tyd.kitprepago.modulo_inventario.dto.response;
import java.math.BigDecimal;
public record ProductoResponse(Long id, String nombre, String descripcion,
    BigDecimal precioRegular, Boolean activo) {}
