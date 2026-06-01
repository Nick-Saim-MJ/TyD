package com.tyd.kitprepago.shared.exception;

public class VentaYaAnuladaException extends KitPrepagoException {
    public VentaYaAnuladaException(Long ventaId) {
        super("La venta " + ventaId + " ya fue anulada previamente");
    }
}
