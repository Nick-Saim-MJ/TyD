package com.tyd.kitprepago.shared.security;

import java.time.Instant;

/**
 * Proyección mínima del Usuario que necesita el módulo shared/security.
 * Implementada por la entidad Usuario del módulo_auth.
 * Evita que shared dependa directamente de la entidad JPA del otro módulo.
 */
public interface UsuarioSecurityProjection {
    Long getId();
    String getUsername();
    String getPasswordHash();
    String getNombreCompleto();
    Rol getRol();
    Long getZonaId();
    Long getSucursalId();
    boolean isActivo();
    Instant getDeletedAt();
    Instant getBloqueadoHasta();
    int getIntentosFallidos();
}