package com.tyd.kitprepago.shared.exception;

public class VentaDuplicadaException extends KitPrepagoException {
    public VentaDuplicadaException(String serieMaestro) {
        super("Ya existe una venta activa para el kit: " + serieMaestro);
    }
}
