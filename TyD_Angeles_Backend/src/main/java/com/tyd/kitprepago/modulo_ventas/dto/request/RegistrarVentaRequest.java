package com.tyd.kitprepago.modulo_ventas.dto.request;

import com.tyd.kitprepago.modulo_ventas.entity.CondicionVenta;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record RegistrarVentaRequest(
    @NotNull Long itemKitId,
    /** Si el cliente ya existe por DNI se usa, si no se crea con los datos de nuevoCliente */
    @NotBlank String clienteDni,
    CrearClienteRequest nuevoCliente,     // null si clienteDni ya existe en BD
    @NotNull Long vendedorId,             // el vendedor que realizó la venta
    @NotNull Long sucursalVentaId,
    @NotNull @DecimalMin("0.01") BigDecimal montoVenta,
    @NotNull CondicionVenta condicion,
    @NotBlank String metodoPago,          // EFECTIVO | YAPE | PLIN | TRANSFERENCIA
    BigDecimal montoRecargaInicial        // opcional para la activación
) {}
