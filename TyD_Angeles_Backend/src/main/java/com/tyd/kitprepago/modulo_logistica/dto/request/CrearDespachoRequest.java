package com.tyd.kitprepago.modulo_logistica.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CrearDespachoRequest(
    @NotNull Long sucursalOrigenId,
    @NotNull Long sucursalDestinoId,
    /** Lista de IDs de items_kit a incluir — deben estar DISPONIBLE en origen */
    @NotEmpty List<Long> itemKitIds,
    /** Número de guía de remisión física (opcional al crear, puede agregarse después) */
    String guiaRemision,
    String observaciones
) {}
