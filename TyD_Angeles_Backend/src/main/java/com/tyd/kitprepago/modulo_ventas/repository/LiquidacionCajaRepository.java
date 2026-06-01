package com.tyd.kitprepago.modulo_ventas.repository;

import com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion;
import com.tyd.kitprepago.modulo_ventas.entity.LiquidacionCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiquidacionCajaRepository extends JpaRepository<LiquidacionCaja, Long> {

    /**
     * Liquidaciones de un vendedor — para VENDEDOR que ve solo las suyas
     */
    List<LiquidacionCaja> findByVendedorIdOrderByFechaLiquidacionDesc(Long vendedorId);

    /**
     * Todas las liquidaciones con filtros — para ADMIN y CONTADOR
     */
    @Query("""

            SELECT lc FROM LiquidacionCaja lc
        JOIN FETCH lc.vendedor u
        JOIN FETCH lc.sucursal sv
        JOIN FETCH sv.zona z
        LEFT JOIN FETCH lc.aprobadoPor
        WHERE (:zonaId     IS NULL OR z.id = :zonaId)
          AND (:vendedorId IS NULL OR u.id = :vendedorId)
          AND (:estado     IS NULL OR lc.estado = :estado)
        ORDER BY lc.fechaLiquidacion DESC
        """)
    List<LiquidacionCaja> findConFiltros(
            @Param("zonaId")     Long zonaId,
            @Param("vendedorId") Long vendedorId,
            @Param("estado") EstadoLiquidacion estado
    );

    /**
     * Verificar si ya existe liquidación activa para ese vendedor/periodo
     */
    @Query("""
        SELECT COUNT(lc) > 0 FROM LiquidacionCaja lc
        WHERE lc.vendedor.id = :vendedorId
          AND lc.estado NOT IN (
              com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion.RECHAZADO
          )
          AND lc.periodoInicio = :inicio
          AND lc.periodoFin    = :fin
        """)
    boolean existeLiquidacionActivaPorPeriodo(
            @Param("vendedorId") Long vendedorId,
            @Param("inicio")     java.time.LocalDate inicio,
            @Param("fin")        java.time.LocalDate fin
    );
}
