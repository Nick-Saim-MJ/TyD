package com.tyd.kitprepago.modulo_reportes.dto.response;

import java.util.List;

public record GenerarKardexResponse(
    String periodo,
    String sucursalNombre,
    int productosCalculados,
    List<KardexResponse> kardex,
    String mensaje
) {}
