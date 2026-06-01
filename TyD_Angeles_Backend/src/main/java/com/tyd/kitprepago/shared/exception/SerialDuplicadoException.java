package com.tyd.kitprepago.shared.exception;

public class SerialDuplicadoException extends KitPrepagoException {
    public SerialDuplicadoException(String serial) {
        super("El serial ya existe en el sistema: " + serial);
    }
}