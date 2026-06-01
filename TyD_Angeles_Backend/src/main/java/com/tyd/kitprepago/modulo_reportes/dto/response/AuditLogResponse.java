package com.tyd.kitprepago.modulo_reportes.dto.response;

import java.time.Instant;

public record AuditLogResponse(
    Long id,
    String tablaNombre,
    Long registroId,
    String accion,
    String datosAnteriores,
    String datosNuevos,
    Long usuarioId,
    String ipAddress,
    Instant fecha
) {}
