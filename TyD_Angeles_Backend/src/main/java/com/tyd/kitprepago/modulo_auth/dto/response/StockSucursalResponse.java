package com.tyd.kitprepago.modulo_auth.dto.response;

/**
 * Respuesta de GET /api/sucursales/{id}/stock
 * Stock en tiempo real — no usa kardex_mensual.
 */
public record StockSucursalResponse(
        Long sucursalId,
        String sucursalNombre,
        Long kitsDisponibles      // COUNT(*) WHERE estado='DISPONIBLE'
) {}
