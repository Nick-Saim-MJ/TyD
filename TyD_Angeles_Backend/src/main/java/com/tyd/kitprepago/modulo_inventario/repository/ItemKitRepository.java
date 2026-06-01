package com.tyd.kitprepago.modulo_inventario.repository;

import com.tyd.kitprepago.modulo_inventario.entity.EstadoKit;
import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemKitRepository extends JpaRepository<ItemKit, Long>,
        JpaSpecificationExecutor<ItemKit> {

    /**
     * Búsqueda por escaneo de código de barras.
     * Busca en los tres seriales en una sola query — toca los tres índices.
     * El almacenero escanea cualquier etiqueta del kit y lo encuentra.
     */
    @Query("""

            SELECT ik FROM ItemKit ik
        LEFT JOIN FETCH ik.producto
        LEFT JOIN FETCH ik.sucursalActual sa
        LEFT JOIN FETCH sa.zona
        WHERE ik.serieMaestro = :serie
           OR ik.serieSim     = :serie
           OR ik.serieDeco    = :serie
        """)
    Optional<ItemKit> findBySerialCualquiera(@Param("serie") String serie);

    /**
     * Todos los kits disponibles en una zona (para el inventario de región).
     * Incluye todas las sucursales de la zona — almacén y oficinas.
     * ZonaContextHolder ya validó que el usuario tiene acceso a esta zona.
     */
    @Query("""
        SELECT ik FROM ItemKit ik
        JOIN FETCH ik.sucursalActual sa
        JOIN FETCH sa.zona z
        LEFT JOIN FETCH ik.producto
        LEFT JOIN FETCH ik.modeloKit
        WHERE z.id = :zonaId
          AND ik.estado = com.tyd.kitprepago.modulo_inventario.entity.EstadoKit.DISPONIBLE
        ORDER BY sa.nombre, ik.serieMaestro
        """)
    List<ItemKit> findDisponiblesPorZona(@Param("zonaId") Long zonaId);

    /**
     * Kits de un lote específico (por numero_operacion / boucher).
     * Solo visible para la zona del almacenero — ZonaContextHolder valida antes.
     */
    @Query("""
        SELECT ik FROM ItemKit ik
        JOIN FETCH ik.lote l
        JOIN FETCH l.zona z
        LEFT JOIN FETCH ik.sucursalActual
        LEFT JOIN FETCH ik.producto
        WHERE l.numeroOperacion = :numeroOperacion
        ORDER BY ik.serieMaestro
        """)
    List<ItemKit> findByBoucher(@Param("numeroOperacion") String numeroOperacion);

    /**
     * Kits de un lote por ID — para GET /api/lotes/{id}/items
     */
    @Query("""
        SELECT ik FROM ItemKit ik
        LEFT JOIN FETCH ik.producto
        LEFT JOIN FETCH ik.modeloKit
        LEFT JOIN FETCH ik.sucursalActual
        LEFT JOIN FETCH ik.custodioActual
        WHERE ik.lote.id = :loteId
        ORDER BY ik.serieMaestro
        """)
    List<ItemKit> findByLoteId(@Param("loteId") Long loteId);

    /**
     * Kits de una sucursal con estado dado — para filtros generales
     */
    Page<ItemKit> findBySucursalActualIdAndEstado(
            Long sucursalId, EstadoKit estado, Pageable pageable);

    /**
     * Para verificar duplicados al ingresar un lote
     */
    boolean existsBySerieMaestro(String serieMaestro);
    boolean existsBySerieSim(String serieSim);
    boolean existsBySerieDecoAndSerieDecoIsNotNull(String serieDeco);

    /**
     * Conteo de stock disponible — alternativa JPA a la native query del módulo auth
     */
    long countBySucursalActualIdAndEstado(Long sucursalId, EstadoKit estado);

    @Query("SELECT COUNT(ik) FROM ItemKit ik WHERE ik.modeloKit.id = :modeloId")
    Long countByModeloKitId(@Param("modeloId") Long modeloId);

}
