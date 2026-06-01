package com.tyd.kitprepago.modulo_reportes.dto.response;

import com.tyd.kitprepago.modulo_ventas.entity.CondicionVenta;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoVenta;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import java.math.BigDecimal;
import java.time.Instant;

/** Una fila del reporte de ventas — usado tanto para JSON como para Excel */
public record ReporteVentaRow(
    Long ventaId,
    String zona, String sucursal,
    String serieMaestro, String serieSim, String producto,
    String clienteDni, String clienteNombre, TipoCliente clienteTipo,
    String vendedor,
    BigDecimal monto, CondicionVenta condicion, String metodoPago,
    EstadoVenta estado,
    BigDecimal montoLiquidado,
    Instant fechaVenta
) {}
