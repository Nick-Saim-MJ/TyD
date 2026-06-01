package com.tyd.kitprepago.shared.exception;

/** El usuario intenta acceder a una zona que no le corresponde. */
public class ZonaNoAutorizadaException extends KitPrepagoException {
    public ZonaNoAutorizadaException(String message) { super(message); }
}
