package com.tyd.kitprepago.modulo_inventario.dto.response;
import com.tyd.kitprepago.modulo_inventario.entity.EstadoKit;
import java.time.Instant;
public record ItemKitResponse(
    Long id,
    Long loteId, String numeroPedido, String numeroOperacion,
    Long productoId, String productoNombre,
    Long modeloKitId, String modeloKitCodigo, Boolean tieneDeco,
    String serieMaestro, String serieSim, String serieDeco,
    EstadoKit estado,
    Long sucursalActualId, String sucursalActualNombre,
    Long zonaId, String zonaNombre,
    String custodioActualNombre,
    Instant fechaIngreso
) {}
