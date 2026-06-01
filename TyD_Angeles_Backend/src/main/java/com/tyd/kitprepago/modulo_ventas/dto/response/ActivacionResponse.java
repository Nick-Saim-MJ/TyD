package com.tyd.kitprepago.modulo_ventas.dto.response;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoActivacion;
import java.math.BigDecimal;
import java.time.Instant;
public record ActivacionResponse(
    Long id,
    Long ventaId,
    String serieSim, String serieMaestro, String productoNombre,
    String clienteNombreCompleto, String clienteDni,
    String vendedorNombre, String sucursalNombre, String zonaNombre,
    BigDecimal montoRecargaInicial,
    EstadoActivacion estado,
    Instant fechaActivacion,
    String comentarios,
    Instant createdAt
) {}
