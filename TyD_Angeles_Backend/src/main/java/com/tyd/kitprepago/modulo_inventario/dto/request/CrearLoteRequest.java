package com.tyd.kitprepago.modulo_inventario.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record CrearLoteRequest(
    @NotBlank String numeroPedido,
    @NotBlank String numeroOperacion,
    @NotNull  Long zonaId,
    @NotNull  Long sucursalRecepcionId,
    @NotNull @Min(1) Integer cantidadEsperada,
    LocalDate fechaPedido,
    LocalDate fechaRecepcion,
    String observaciones,

    /** Kits que llegan en este lote. Puede enviarse vacío y cargarlos después. */
    @Valid List<ItemKitIngresoRequest> items
) {}
