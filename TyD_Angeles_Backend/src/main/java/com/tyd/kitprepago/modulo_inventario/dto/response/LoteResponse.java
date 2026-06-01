package com.tyd.kitprepago.modulo_inventario.dto.response;
import java.time.Instant;
import java.time.LocalDate;
public record LoteResponse(
    Long id,
    String numeroPedido,
    String numeroOperacion,
    Long zonaId, String zonaNombre, String zonaCodigoDirecTV,
    Long sucursalRecepcionId, String sucursalRecepcionNombre,
    Integer cantidadEsperada, Integer cantidadRecibida,
    LocalDate fechaPedido, LocalDate fechaRecepcion,
    String observaciones,
    String usuarioRegistroNombre,
    Instant createdAt
) {}
