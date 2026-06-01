package com.tyd.kitprepago.shared.exception;

public class KitPrepagoException extends RuntimeException {
    public KitPrepagoException(String message) { super(message); }
    public KitPrepagoException(String message, Throwable cause) { super(message, cause); }
}
