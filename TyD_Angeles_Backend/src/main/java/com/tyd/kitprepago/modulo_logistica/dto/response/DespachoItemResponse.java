package com.tyd.kitprepago.modulo_logistica.dto.response;

import com.tyd.kitprepago.modulo_logistica.entity.EstadoDespachoItem;

public record DespachoItemResponse(
    Long id,
    Long itemKitId,
    String serieMaestro,
    String serieSim,
    String serieDeco,
    String productoNombre,
    String sucursalActualNombre,
    EstadoDespachoItem estadoItem,
    String observacion
) {}
