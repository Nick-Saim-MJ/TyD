package com.tyd.kitprepago.modulo_auth.repository.impl;

import com.tyd.kitprepago.modulo_auth.repository.UsuarioJpaRepository;
import com.tyd.kitprepago.shared.security.UsuarioSecurityProjection;
import com.tyd.kitprepago.shared.security.UsuarioSecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Implementa la interfaz que shared/security necesita para operar.
 * Este es el único punto de acoplamiento entre shared y modulo_auth.
 * <p>
 * IMPORTANTE: no anotar con @Repository — Spring lo detecta como @Service.
 * Esto evita conflicto de nombres con UsuarioJpaRepository.
 */
@Service
@RequiredArgsConstructor
class UsuarioSecurityRepositoryImpl implements UsuarioSecurityRepository {

    private final UsuarioJpaRepository jpa;

    @Override
    public Optional<UsuarioSecurityProjection> findByUsernameForAuth(String username) {
        // Usuario implementa UsuarioSecurityProjection directamente
        return jpa.findByUsername(username).map(u -> (UsuarioSecurityProjection) u);
    }

    @Override
    @Transactional
    public void incrementarIntentosFallidos(Long usuarioId, int nuevoValor) {
        jpa.updateIntentosFallidos(usuarioId, nuevoValor);
    }

    @Override
    @Transactional
    public void bloquearCuenta(Long usuarioId, Instant bloqueadoHasta, int intentos) {
        jpa.updateBloqueo(usuarioId, bloqueadoHasta, intentos);
    }

    @Override
    @Transactional
    public void resetearBloqueo(Long usuarioId, Instant ultimoLogin) {
        jpa.updateLoginExitoso(usuarioId, ultimoLogin);
    }
}
