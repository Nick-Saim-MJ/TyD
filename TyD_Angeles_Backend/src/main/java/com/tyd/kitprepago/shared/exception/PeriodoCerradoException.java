package com.tyd.kitprepago.shared.exception;

public class PeriodoCerradoException extends KitPrepagoException {
    public PeriodoCerradoException(String periodo) {
        super("El periodo " + periodo + " está cerrado. Solo ADMIN puede reabrirlo");
    }
}

