package com.tyd.kitprepago.shared.security;

import java.time.Instant;
import java.util.Optional;

/**
 * Contrato de acceso a datos que necesita shared/security.
 * El módulo_auth provee la implementación a través de su JpaRepository.
 *
 * Por qué no usar el JpaRepository directamente:
 * - shared no debe tener dependencia de módulos de negocio
 * - Permite mockear fácilmente en tests del shared layer
 */
public interface UsuarioSecurityRepository {

    Optional<UsuarioSecurityProjection> findByUsernameForAuth(String username);

    void incrementarIntentosFallidos(Long usuarioId, int nuevoValor);

    void bloquearCuenta(Long usuarioId, Instant bloqueadoHasta, int intentos);

    /** Resetea intentos_fallidos=0, bloqueado_hasta=NULL y actualiza ultimo_login */
    void resetearBloqueo(Long usuarioId, Instant ultimoLogin);
}
