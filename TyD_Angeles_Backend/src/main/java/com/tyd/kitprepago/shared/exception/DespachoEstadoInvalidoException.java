package com.tyd.kitprepago.shared.exception;

public class DespachoEstadoInvalidoException extends KitPrepagoException {
    public DespachoEstadoInvalidoException(Long despachoId, String estadoActual, String operacion) {
        super(String.format("No se puede %s el despacho %d en estado %s",
                operacion, despachoId, estadoActual));
    }
}
