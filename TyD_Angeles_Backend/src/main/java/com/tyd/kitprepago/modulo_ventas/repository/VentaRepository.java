package com.tyd.kitprepago.modulo_ventas.repository;

import com.tyd.kitprepago.modulo_ventas.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Historial de ventas con filtros opcionales.
     * ZonaContextHolder decide los parámetros en el servicio:
     *  - VENDEDOR:      sucursalId = su sucursal
     *  - ALMACENERO:    zonaId = su zona  (sucursalId = null)
     *  - JEFE/ADMIN/CONTADOR: zonaId = null, sucursalId = null → todo
     */
    @Query("""
        SELECT v FROM Venta v
        JOIN FETCH v.itemKit ik
        JOIN FETCH ik.producto
        JOIN FETCH v.cliente c
        JOIN FETCH v.vendedor u
        JOIN FETCH v.sucursalVenta sv
        JOIN FETCH sv.zona z
        WHERE (:sucursalId IS NULL OR sv.id = :sucursalId)
          AND (:zonaId     IS NULL OR z.id = :zonaId)
          AND (:vendedorId IS NULL OR u.id = :vendedorId)
          AND (:tipoCliente IS NULL OR c.tipo = :tipoCliente)
          AND (:desde IS NULL OR v.fechaVenta >= :desde)
          AND (:hasta IS NULL OR v.fechaVenta <= :hasta)
          AND (:soloActivas = false OR v.estado = com.tyd.kitprepago.modulo_ventas.entity.EstadoVenta.ACTIVA)
        ORDER BY v.fechaVenta DESC
        """)
    List<Venta> findConFiltros(
            @Param("sucursalId")  Long sucursalId,
            @Param("zonaId")      Long zonaId,
            @Param("vendedorId")  Long vendedorId,
            @Param("tipoCliente") TipoCliente tipoCliente,
            @Param("desde")       Instant desde,
            @Param("hasta")       Instant hasta,
            @Param("soloActivas") boolean soloActivas
    );

    /**
     * Ventas ACTIVAS de un vendedor en un periodo que aún no tienen liquidación.
     * Usado para calcular monto_total_esperado al crear una liquidación.
     */
    @Query("""
        SELECT v FROM Venta v
        WHERE v.vendedor.id = :vendedorId
          AND v.estado = com.tyd.kitprepago.modulo_ventas.entity.EstadoVenta.ACTIVA
          AND v.liquidacion IS NULL
          AND v.fechaVenta >= :desde
          AND v.fechaVenta <= :hasta
        """)
    List<Venta> findNoLiquidadasPorVendedor(
            @Param("vendedorId") Long vendedorId,
            @Param("desde")      Instant desde,
            @Param("hasta")      Instant hasta
    );

    /** Suma de montos de ventas activas no liquidadas de un vendedor en un periodo */
    @Query("""
        SELECT COALESCE(SUM(v.montoVenta), 0)
        FROM Venta v
        WHERE v.vendedor.id = :vendedorId
          AND v.estado = com.tyd.kitprepago.modulo_ventas.entity.EstadoVenta.ACTIVA
          AND v.liquidacion IS NULL
          AND v.fechaVenta >= :desde
          AND v.fechaVenta <= :hasta
        """)
    BigDecimal sumMontoNoLiquidado(
            @Param("vendedorId") Long vendedorId,
            @Param("desde")      Instant desde,
            @Param("hasta")      Instant hasta
    );

    /** Asignar liquidación a ventas del periodo al aprobar */
    @Modifying
    @Query("""
        UPDATE Venta v
        SET v.liquidacion = :liquidacion, v.montoLiquidado = v.montoVenta
        WHERE v.vendedor.id = :vendedorId
          AND v.estado = com.tyd.kitprepago.modulo_ventas.entity.EstadoVenta.ACTIVA
          AND v.liquidacion IS NULL
          AND v.fechaVenta >= :desde
          AND v.fechaVenta <= :hasta
        """)
    int asignarLiquidacion(
            @Param("vendedorId")   Long vendedorId,
            @Param("liquidacion")  LiquidacionCaja liquidacion,
            @Param("desde")        Instant desde,
            @Param("hasta")        Instant hasta
    );
}