package com.tyd.kitprepago.shared.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Implementación de UserDetails que lleva los datos de negocio
 * que necesitan los guards y el ZonaContextHolder.
 *
 * Spring Security solo conoce username/password/authorities.
 * Extendemos con rol, zonaId y sucursalId para no hacer queries
 * adicionales a BD en cada request — estos vienen del JWT.
 */
@Getter
@Builder
public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String nombreCompleto;
    private final Rol rol;

    /**
     * NULL para ADMIN y JEFE_ALMACEN (operan en múltiples zonas).
     * Siempre presente para ALMACENERO, VENDEDOR y CONTADOR.
     */
    private final Long zonaId;

    /**
     * NULL para ADMIN, JEFE_ALMACEN y CONTADOR.
     * Presente para ALMACENERO y VENDEDOR.
     */
    private final Long sucursalId;

    private final boolean activo;

    // Spring Security usa "ROLE_" como prefijo por convención
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return activo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
