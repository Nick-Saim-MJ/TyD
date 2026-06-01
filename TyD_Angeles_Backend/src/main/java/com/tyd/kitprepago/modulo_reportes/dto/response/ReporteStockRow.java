package com.tyd.kitprepago.modulo_reportes.dto.response;

/** Snapshot de inventario por sucursal — usado en GET /api/reportes/stock */
public record ReporteStockRow(
    String zona, String sucursal, String tipoSucursal,
    String producto,
    long disponibles, long enTransito, long defectuosos, long vendidos,
    long totalKits
) {}
