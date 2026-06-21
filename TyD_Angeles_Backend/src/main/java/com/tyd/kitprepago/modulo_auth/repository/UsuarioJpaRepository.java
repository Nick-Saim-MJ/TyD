package com.tyd.kitprepago.modulo_auth.repository;

import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.shared.security.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

// ═══════════════════════════════════════════════════════════════════════════
// JPA REPOSITORY
// ═══════════════════════════════════════════════════════════════════════════

@Repository
public interface UsuarioJpaRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Todos los usuarios de una zona — para el admin y los listados filtrados */
    List<Usuario> findByZona_IdAndDeletedAtIsNull(Long zonaId);

    /**
     * Vendedores y almaceneros de una zona para el autocomplete de ventas.
     * Excluye a los vendedores freelance (sucursal_id NULL) — ya no se ofrecen
     * en el autocompletado, aunque el registro siga existiendo en la BD.
     */
    List<Usuario> findByZona_IdAndRolInAndActivoTrueAndDeletedAtIsNullAndSucursalIsNotNull(
            Long zonaId, List<Rol> roles);

    /** Todos los usuarios activos (solo ADMIN) */
    List<Usuario> findByActivoTrueAndDeletedAtIsNull();

    // ── Seguridad: bloqueo por fuerza bruta ─────────────────────────────

    @Modifying
    @Query("UPDATE Usuario u SET u.intentosFallidos = :intentos WHERE u.id = :id")
    void updateIntentosFallidos(@Param("id") Long id, @Param("intentos") int intentos);

    @Modifying
    @Query("UPDATE Usuario u SET u.bloqueadoHasta = :hasta, u.intentosFallidos = :intentos WHERE u.id = :id")
    void updateBloqueo(@Param("id") Long id,
                       @Param("hasta") Instant bloqueadoHasta,
                       @Param("intentos") int intentos);

    @Modifying
    @Query("UPDATE Usuario u SET u.intentosFallidos = 0, u.bloqueadoHasta = NULL, u.ultimoLogin = :login WHERE u.id = :id")
    void updateLoginExitoso(@Param("id") Long id, @Param("login") Instant ultimoLogin);

    @Modifying
    @Query("UPDATE Usuario u SET u.deletedAt = :now WHERE u.id = :id")
    void softDelete(@Param("id") Long id, @Param("now") Instant now);

    List<Usuario> findByZona_IdAndSucursal_IdAndRolInAndActivoTrueAndDeletedAtIsNull(Long zonaId, Long sucursalId, List<Rol> vendedor);
}

// ═══════════════════════════════════════════════════════════════════════════
// IMPLEMENTACIÓN DEL CONTRATO shared/security/UsuarioSecurityRepository
// ═══════════════════════════════════════════════════════════════════════════

