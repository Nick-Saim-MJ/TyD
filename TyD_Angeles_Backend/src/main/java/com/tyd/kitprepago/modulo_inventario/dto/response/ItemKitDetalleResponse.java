package com.tyd.kitprepago.modulo_inventario.dto.response;
import com.tyd.kitprepago.modulo_inventario.entity.EstadoKit;
import java.time.Instant;
import java.util.List;
/** Respuesta enriquecida para el endpoint de línea de vida del kit */
public record ItemKitDetalleResponse(
    Long id,
    String serieMaestro, String serieSim, String serieDeco,
    EstadoKit estado,
    String sucursalActualNombre,
    String zonaNombre,
    String productoNombre,
    String modeloKitCodigo,
    Instant fechaIngreso,
    List<HistorialCustodioResponse> historial
) {}
