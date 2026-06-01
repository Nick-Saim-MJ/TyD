package com.tyd.kitprepago.modulo_ventas.dto.response;
import com.tyd.kitprepago.modulo_ventas.entity.CondicionVenta;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoVenta;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import java.math.BigDecimal;
import java.time.Instant;
public record VentaResponse(
    Long id,
    Long itemKitId, String serieMaestro, String serieSim, String productoNombre,
    Long clienteId, String clienteNombreCompleto, String clienteDni, TipoCliente clienteTipo,
    Long vendedorId, String vendedorNombre,
    Long sucursalVentaId, String sucursalVentaNombre, String zonaNombre,
    BigDecimal montoVenta, BigDecimal montoLiquidado,
    CondicionVenta condicion, String metodoPago,
    EstadoVenta estado, String motivoAnulacion,
    Long liquidacionId,
    Instant fechaVenta
) {}
