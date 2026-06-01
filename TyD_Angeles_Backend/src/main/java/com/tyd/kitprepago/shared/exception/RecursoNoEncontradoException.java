package com.tyd.kitprepago.shared.exception;

/** Entidad no encontrada. Genérica para cualquier tabla. */
public class RecursoNoEncontradoException extends KitPrepagoException {
    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " no encontrado con ID: " + id);
    }
    public RecursoNoEncontradoException(String message) { super(message); }
}
