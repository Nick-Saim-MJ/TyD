package com.tyd.kitprepago.shared.zona;

import com.tyd.kitprepago.shared.exception.ZonaNoAutorizadaException;
import com.tyd.kitprepago.shared.security.Rol;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resuelve automáticamente el filtro de zona para el usuario autenticado.
 *
 * Lógica de acceso:
 *  - ADMIN y CONTADOR → Optional.empty() = sin filtro, ven todo
 *  - JEFE_ALMACEN     → Optional.of(zonaId) = solo su zona asignada
 *  - ALMACENERO       → Optional.of(zonaId) = solo su zona
 *  - VENDEDOR         → Optional.of(zonaId) = solo su zona
 *
 * Uso en servicios:
 *   zonaContextHolder.getZonaIdFiltro()
 *       .ifPresent(zonaId -> query.setParameter("zonaId", zonaId));
 *
 * Uso en repositorios con Specification:
 *   zonaContextHolder.aplicarFiltro(spec) // agrega WHERE zona_id = :x si aplica
 */
@Component
public class ZonaContextHolder {

    /**
     * Retorna el zonaId que debe usarse como filtro en las queries.
     * Optional.empty() = sin filtro (ADMIN/CONTADOR ven todo).
     * Optional.of(id)  = filtrar por esa zona.
     */
    public Optional<Long> getZonaIdFiltro() {
        UsuarioPrincipal principal = getPrincipal();
        if (principal == null) return Optional.empty();

        return switch (principal.getRol()) {
            case ADMIN, CONTADOR -> Optional.empty();
            case JEFE_ALMACEN, ALMACENERO, VENDEDOR ->
                    Optional.ofNullable(principal.getZonaId());
        };
    }

    /**
     * Retorna el sucursalId del usuario para filtros específicos por sucursal.
     * Solo aplica para VENDEDOR (ve solo su sucursal en el historial de ventas).
     */
    public Optional<Long> getSucursalIdFiltro() {
        UsuarioPrincipal principal = getPrincipal();
        if (principal == null) return Optional.empty();

        return switch (principal.getRol()) {
            case VENDEDOR -> Optional.ofNullable(principal.getSucursalId());
            default -> Optional.empty();
        };
    }

    /**
     * Verifica si el usuario puede acceder a una zona específica.
     * Útil para el ZonaGuard del backend antes de operaciones de escritura.
     *
     * @param zonaId La zona a verificar
     * @return true si el usuario tiene acceso
     * @throws ZonaNoAutorizadaException si no tiene acceso
     */
    public boolean puedeAccederZona(Long zonaId) {
        UsuarioPrincipal principal = getPrincipal();
        if (principal == null) return false;

        return switch (principal.getRol()) {
            case ADMIN, CONTADOR -> true;
            case JEFE_ALMACEN, ALMACENERO, VENDEDOR ->
                    zonaId.equals(principal.getZonaId());
        };
    }

    /**
     * Lanza ZonaNoAutorizadaException si el usuario no puede acceder.
     * Usar en services antes de operaciones sensibles.
     */
    public void validarAccesoZona(Long zonaId) {
        if (!puedeAccederZona(zonaId)) {
            throw new com.tyd.kitprepago.shared.exception.ZonaNoAutorizadaException(
                    "No tienes acceso a la zona con ID: " + zonaId
            );
        }
    }

    /**
     * Retorna el usuario autenticado actualmente.
     */
    public UsuarioPrincipal getPrincipalActual() {
        return getPrincipal();
    }

    /**
     * Retorna el ID del usuario autenticado.
     * Útil para registrar created_by_id en entidades.
     */
    public Long getUsuarioActualId() {
        UsuarioPrincipal principal = getPrincipal();
        return principal != null ? principal.getId() : null;
    }

    public Rol getRolActual() {
        UsuarioPrincipal principal = getPrincipal();
        return principal != null ? principal.getRol() : null;
    }

    private UsuarioPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        if (auth.getPrincipal() instanceof UsuarioPrincipal up) return up;
        return null;
    }
}