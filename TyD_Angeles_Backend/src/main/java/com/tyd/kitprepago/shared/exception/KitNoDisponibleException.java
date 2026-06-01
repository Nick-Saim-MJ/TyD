package com.tyd.kitprepago.shared.exception;

/**
 * Se lanza cuando se intenta vender o despachar un kit que no está DISPONIBLE.
 * El campo estadoActual ayuda al frontend a mostrar un mensaje descriptivo.
 */
public class KitNoDisponibleException extends KitPrepagoException {
    private final String serieMaestro;
    private final String estadoActual;
    public KitNoDisponibleException(String serieMaestro, String estadoActual) {
        super(String.format("El kit %s no está disponible. Estado actual: %s",
                serieMaestro, estadoActual));
        this.serieMaestro = serieMaestro;
        this.estadoActual = estadoActual;
    }
    public String getSerieMaestro() { return serieMaestro; }
    public String getEstadoActual() { return estadoActual; }
}
