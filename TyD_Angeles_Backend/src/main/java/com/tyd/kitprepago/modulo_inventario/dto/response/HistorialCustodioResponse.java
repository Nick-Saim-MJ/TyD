package com.tyd.kitprepago.modulo_inventario.dto.response;
import com.tyd.kitprepago.modulo_inventario.entity.TipoEvento;
import java.time.Instant;
public record HistorialCustodioResponse(
    Long id,
    String sucursalAnteriorNombre,
    String sucursalNuevaNombre,
    String custodioAnteriorNombre,
    String custodioNuevoNombre,
    TipoEvento tipoEvento,
    String motivo,
    Long referenciaId,
    String referenciaTipo,
    String registradoPorNombre,
    Instant fechaEvento
) {}
