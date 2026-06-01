package com.tyd.kitprepago.modulo_auth.repository;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SucursalJpaRepository extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByZonaIdAndDeletedAtIsNull(Long zonaId);

    List<Sucursal> findByDeletedAtIsNull();

    /**
     * Stock en tiempo real de una sucursal.
     * Native query para no crear dependencia JPA con la entidad ItemKit
     * del módulo de inventario. El resultado es un Long simple.
     *
     * La @SQLRestriction de Sucursal no afecta esta query ya que
     * filtramos items_kit, no sucursales.
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM items_kit ik
        WHERE ik.sucursal_actual_id = :sucursalId
          AND ik.estado = 'DISPONIBLE'
        """, nativeQuery = true)
    Long contarStockDisponible(@Param("sucursalId") Long sucursalId);

    /**
     * Sucursales que comparten ubicación física con una dada.
     * Útil para mostrar kits del almacén en la vista de la oficina vinculada.
     */
    @Query("SELECT s FROM Sucursal s WHERE s.ubicacionFisica.id = :sucursalId OR s.id = :sucursalId")
    List<Sucursal> findSucursalesEnMismaUbicacion(@Param("sucursalId") Long sucursalId);
}
