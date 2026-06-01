package com.tyd.kitprepago.modulo_ventas.dto.response;
import java.time.Instant;
/** Respuesta enriquecida al registrar una nueva venta */
public record RegistrarVentaResponse(
    Long ventaId,
    Long activacionId,
    String serieMaestro, String serieSim,
    String clienteNombreCompleto,
    String mensaje,
    Instant fechaVenta
) {}
