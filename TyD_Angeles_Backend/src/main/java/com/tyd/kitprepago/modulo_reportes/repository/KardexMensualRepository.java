package com.tyd.kitprepago.modulo_reportes.repository;

import com.tyd.kitprepago.modulo_reportes.entity.KardexMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

// ─────────────────────────────────────────────────────────────────────────────
// KARDEX MENSUAL
// ─────────────────────────────────────────────────────────────────────────────

@Repository
public interface KardexMensualRepository extends JpaRepository<KardexMensual, Long> {

    Optional<KardexMensual> findBySucursalIdAndProductoIdAndPeriodo(
            Long sucursalId, Long productoId, String periodo);

    List<KardexMensual> findBySucursalIdAndPeriodoOrderByProductoNombre(
            Long sucursalId, String periodo);

    /** Consulta del contador: kardex de todas las sucursales en un periodo */
    @Query("""
        SELECT k FROM KardexMensual k
        JOIN FETCH k.sucursal s
        JOIN FETCH s.zona z
        JOIN FETCH k.producto p
        LEFT JOIN FETCH k.generadoPor
        WHERE (:zonaId     IS NULL OR z.id  = :zonaId)
          AND (:sucursalId IS NULL OR s.id  = :sucursalId)
          AND (:periodo    IS NULL OR k.periodo = :periodo)
        ORDER BY z.nombre, s.nombre, k.periodo DESC, p.nombre
        """)
    List<KardexMensual> findConFiltros(
            @Param("zonaId")     Long zonaId,
            @Param("sucursalId") Long sucursalId,
            @Param("periodo")    String periodo
    );

    // ── Cálculos para generación del kardex desde historial_custodios ─────────

    /**
     * Stock final del periodo anterior — se convierte en stock_inicio del nuevo.
     * Busca el kardex más reciente antes del periodo indicado.
     */
    @Query("""
        SELECT k.stockFin FROM KardexMensual k
        WHERE k.sucursal.id = :sucursalId
          AND k.producto.id = :productoId
          AND k.periodo     < :periodo
        ORDER BY k.periodo DESC
        LIMIT 1
        """)
    Optional<Integer> findStockFinPeriodoAnterior(
            @Param("sucursalId") Long sucursalId,
            @Param("productoId") Long productoId,
            @Param("periodo")    String periodo
    );

    /**
     * Cuenta los INGRESOS a una sucursal en un periodo desde historial_custodios.
     * Incluye: tipo INGRESO con sucursal_nueva = sucursalId
     *        + tipo TRASLADO con sucursal_nueva = sucursalId
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM historial_custodios hc
        JOIN items_kit ik ON hc.item_kit_id = ik.id
        WHERE hc.sucursal_nueva_id = :sucursalId
          AND ik.producto_id       = :productoId
          AND hc.tipo_evento       IN ('INGRESO','TRASLADO','DEVOLUCION')
          AND hc.fecha_evento     >= :desde
          AND hc.fecha_evento     <= :hasta
        """, nativeQuery = true)
    int contarIngresosSucursal(
            @Param("sucursalId") Long sucursalId,
            @Param("productoId") Long productoId,
            @Param("desde")      Instant desde,
            @Param("hasta")      Instant hasta
    );

    /**
     * Cuenta las SALIDAS de una sucursal en un periodo desde historial_custodios.
     * Incluye: tipo VENTA + tipo BAJA + tipo TRASLADO con sucursal_anterior = sucursalId
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM historial_custodios hc
        JOIN items_kit ik ON hc.item_kit_id = ik.id
        WHERE hc.sucursal_anterior_id = :sucursalId
          AND ik.producto_id          = :productoId
          AND hc.tipo_evento          IN ('VENTA','BAJA','TRASLADO')
          AND hc.fecha_evento        >= :desde
          AND hc.fecha_evento        <= :hasta
        """, nativeQuery = true)
    int contarSalidasSucursal(
            @Param("sucursalId") Long sucursalId,
            @Param("productoId") Long productoId,
            @Param("desde")      Instant desde,
            @Param("hasta")      Instant hasta
    );

    /**
     * Suma de montos de ventas activas para una sucursal en el periodo.
     * Se usa como total_liquidado en el kardex.
     */
    @Query(value = """
        SELECT COALESCE(SUM(v.monto_venta), 0)
        FROM ventas v
        WHERE v.sucursal_venta_id = :sucursalId
          AND v.estado            = 'ACTIVA'
          AND v.fecha_venta      >= :desde
          AND v.fecha_venta      <= :hasta
        """, nativeQuery = true)
    BigDecimal sumMontoVentasSucursal(
            @Param("sucursalId") Long sucursalId,
            @Param("desde")      Instant desde,
            @Param("hasta")      Instant hasta
    );
}

// ─────────────────────────────────────────────────────────────────────────────
// AUDIT LOG — solo lectura en este módulo
// ─────────────────────────────────────────────────────────────────────────────

