package com.tyd.kitprepago.shared.exception;

import java.time.Instant;

/** Cuenta bloqueada por intentos fallidos. Lleva la fecha de desbloqueo. */
public class CuentaBloqueadaException extends KitPrepagoException {
    private final Instant bloqueadoHasta;
    public CuentaBloqueadaException(Instant bloqueadoHasta) {
        super("Cuenta bloqueada temporalmente por múltiples intentos fallidos");
        this.bloqueadoHasta = bloqueadoHasta;
    }
    public Instant getBloqueadoHasta() { return bloqueadoHasta; }
}

