package com.tyd.kitprepago.modulo_auth.repository;

import com.tyd.kitprepago.modulo_auth.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// ─────────────────────────────────────────────────────────────────────────────
// ZONA
// ─────────────────────────────────────────────────────────────────────────────

@Repository
interface ZonaJpaRepository extends JpaRepository<Zona, Long> {
    Optional<Zona> findByCodigoZona(String codigoZona);
    boolean existsByCodigoZona(String codigoZona);
    List<Zona> findByActivoTrue();
}
