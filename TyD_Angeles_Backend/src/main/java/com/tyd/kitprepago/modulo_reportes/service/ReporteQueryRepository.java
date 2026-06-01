package com.tyd.kitprepago.modulo_reportes.service;

import com.tyd.kitprepago.modulo_reportes.dto.response.*;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Repositorio de queries nativas para los reportes.
 *
 * Se usa JDBC directo (NamedParameterJdbcTemplate) en lugar de JPA porque:
 * 1. Los reportes hacen JOINs entre múltiples módulos (ventas + inventario + auth)
 * 2. Los ResultSets se mapean directamente a Records inmutables
 * 3. Mejor control del SQL para optimización del contador
 *
 * Todas las queries son READ ONLY — este repositorio nunca escribe.
 */
@Repository
@RequiredArgsConstructor
public class ReporteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ─────────────────────── VENTAS ──────────────────────────────────────────

    public List<ReporteVentaRow> ventasParaReporte(Long zonaId, Long sucursalId,
                                                    Long vendedorId, TipoCliente tipoCliente,
                                                    Instant desde, Instant hasta) {
        var params = new MapSqlParameterSource()
                .addValue("zonaId",      zonaId)
                .addValue("sucursalId",  sucursalId)
                .addValue("vendedorId",  vendedorId)
                .addValue("tipoCliente", tipoCliente != null ? tipoCliente.name() : null)
                .addValue("desde",       desde != null ? Timestamp.from(desde) : null)
                .addValue("hasta",       hasta != null ? Timestamp.from(hasta) : null);

        String sql = """
            SELECT v.id          AS ventaId,
                   z.nombre      AS zona,
                   sv.nombre     AS sucursal,
                   ik.serie_maestro, ik.serie_sim,
                   p.nombre      AS producto,
                   c.dni         AS clienteDni,
                   CONCAT(c.nombres, ' ', c.apellidos) AS clienteNombre,
                   c.tipo        AS clienteTipo,
                   u.nombre_completo AS vendedor,
                   v.monto_venta, v.condicion, v.metodo_pago,
                   v.estado,
                   v.monto_liquidado,
                   v.fecha_venta
            FROM ventas v
            JOIN items_kit ik   ON v.item_kit_id      = ik.id
            JOIN productos p    ON ik.producto_id     = p.id
            JOIN clientes c     ON v.cliente_id       = c.id
            JOIN usuarios u     ON v.vendedor_id      = u.id
            JOIN sucursales sv  ON v.sucursal_venta_id = sv.id
            JOIN zonas z        ON sv.zona_id         = z.id
            WHERE (:zonaId      IS NULL OR z.id        = :zonaId)
              AND (:sucursalId  IS NULL OR sv.id        = :sucursalId)
              AND (:vendedorId  IS NULL OR u.id         = :vendedorId)
              AND (:tipoCliente IS NULL OR c.tipo        = :tipoCliente)
              AND (:desde       IS NULL OR v.fecha_venta >= :desde)
              AND (:hasta       IS NULL OR v.fecha_venta <= :hasta)
            ORDER BY v.fecha_venta DESC
            """;

        return jdbc.query(sql, params, (rs, i) -> new ReporteVentaRow(
                rs.getLong("ventaId"),
                rs.getString("zona"),
                rs.getString("sucursal"),
                rs.getString("serie_maestro"),
                rs.getString("serie_sim"),
                rs.getString("producto"),
                rs.getString("clienteDni"),
                rs.getString("clienteNombre"),
                safeEnum(TipoCliente.class, rs.getString("clienteTipo")),
                rs.getString("vendedor"),
                rs.getBigDecimal("monto_venta"),
                safeEnum(com.tyd.kitprepago.modulo_ventas.entity.CondicionVenta.class, rs.getString("condicion")),
                rs.getString("metodo_pago"),
                safeEnum(com.tyd.kitprepago.modulo_ventas.entity.EstadoVenta.class, rs.getString("estado")),
                rs.getBigDecimal("monto_liquidado"),
                toInstant(rs.getTimestamp("fecha_venta"))
        ));
    }

    // ─────────────────────── STOCK ────────────────────────────────────────────

    public List<ReporteStockRow> stockPorSucursal(Long zonaId) {
        var params = new MapSqlParameterSource().addValue("zonaId", zonaId);

        String sql = """
            SELECT z.nombre      AS zona,
                   sv.nombre     AS sucursal,
                   sv.tipo       AS tipoSucursal,
                   p.nombre      AS producto,
                   SUM(CASE WHEN ik.estado = 'DISPONIBLE'  THEN 1 ELSE 0 END) AS disponibles,
                   SUM(CASE WHEN ik.estado = 'TRANSITO'    THEN 1 ELSE 0 END) AS enTransito,
                   SUM(CASE WHEN ik.estado = 'DEFECTUOSO'  THEN 1 ELSE 0 END) AS defectuosos,
                   SUM(CASE WHEN ik.estado = 'VENDIDO'     THEN 1 ELSE 0 END) AS vendidos,
                   COUNT(*)                                                     AS totalKits
            FROM items_kit ik
            JOIN sucursales sv ON ik.sucursal_actual_id = sv.id
            JOIN zonas z       ON sv.zona_id            = z.id
            JOIN productos p   ON ik.producto_id        = p.id
            WHERE (:zonaId IS NULL OR z.id = :zonaId)
              AND sv.deleted_at IS NULL
            GROUP BY z.nombre, sv.nombre, sv.tipo, p.nombre
            ORDER BY z.nombre, sv.nombre, p.nombre
            """;

        return jdbc.query(sql, params, (rs, i) -> new ReporteStockRow(
                rs.getString("zona"),
                rs.getString("sucursal"),
                rs.getString("tipoSucursal"),
                rs.getString("producto"),
                rs.getLong("disponibles"),
                rs.getLong("enTransito"),
                rs.getLong("defectuosos"),
                rs.getLong("vendidos"),
                rs.getLong("totalKits")
        ));
    }

    // ─────────────────────── ACTIVACIONES PENDIENTES ─────────────────────────

    public List<ReporteActivacionPendienteRow> activacionesPendientes(Long zonaId) {
        var params = new MapSqlParameterSource().addValue("zonaId", zonaId);

        String sql = """
            SELECT v.id           AS ventaId,
                   a.id           AS activacionId,
                   z.nombre       AS zona,
                   sv.nombre      AS sucursal,
                   ik.serie_sim, ik.serie_maestro,
                   p.nombre       AS producto,
                   c.dni          AS clienteDni,
                   CONCAT(c.nombres, ' ', c.apellidos) AS clienteNombre,
                   u.nombre_completo AS vendedor,
                   v.monto_venta,
                   a.monto_recarga_inicial,
                   v.fecha_venta
            FROM activaciones a
            JOIN ventas v      ON a.venta_id          = v.id
            JOIN items_kit ik  ON v.item_kit_id        = ik.id
            JOIN productos p   ON ik.producto_id       = p.id
            JOIN clientes c    ON v.cliente_id         = c.id
            JOIN usuarios u    ON v.vendedor_id        = u.id
            JOIN sucursales sv ON v.sucursal_venta_id  = sv.id
            JOIN zonas z       ON sv.zona_id           = z.id
            WHERE a.estado = 'PENDIENTE'
              AND v.estado = 'ACTIVA'
              AND (:zonaId IS NULL OR z.id = :zonaId)
            ORDER BY v.fecha_venta ASC
            """;

        return jdbc.query(sql, params, (rs, i) -> new ReporteActivacionPendienteRow(
                rs.getLong("ventaId"),
                rs.getLong("activacionId"),
                rs.getString("zona"),
                rs.getString("sucursal"),
                rs.getString("serie_sim"),
                rs.getString("serie_maestro"),
                rs.getString("producto"),
                rs.getString("clienteDni"),
                rs.getString("clienteNombre"),
                rs.getString("vendedor"),
                rs.getBigDecimal("monto_venta"),
                rs.getBigDecimal("monto_recarga_inicial"),
                toInstant(rs.getTimestamp("fecha_venta")),
                0L   // diasSinActivar se calcula en ReporteService
        ));
    }

    // ─────────────────────── LIQUIDACIONES ───────────────────────────────────

    public List<ReporteLiquidacionRow> liquidacionesParaReporte(Long zonaId,
                                                                  String periodoInicio,
                                                                  String periodoFin,
                                                                  EstadoLiquidacion estado) {
        var params = new MapSqlParameterSource()
                .addValue("zonaId",        zonaId)
                .addValue("periodoInicio", periodoInicio)
                .addValue("periodoFin",    periodoFin)
                .addValue("estado",        estado != null ? estado.name() : null);

        String sql = """
            SELECT lc.id AS liquidacionId,
                   z.nombre  AS zona,
                   sv.nombre AS sucursal,
                   u.nombre_completo AS vendedor,
                   lc.periodo_inicio, lc.periodo_fin,
                   lc.monto_total_esperado AS montoEsperado,
                   lc.monto_depositado     AS montoDepositado,
                   lc.diferencia,
                   lc.estado,
                   ap.nombre_completo AS aprobadoPor,
                   lc.fecha_liquidacion, lc.fecha_aprobacion
            FROM liquidaciones_caja lc
            JOIN usuarios u     ON lc.vendedor_id   = u.id
            JOIN sucursales sv  ON lc.sucursal_id   = sv.id
            JOIN zonas z        ON sv.zona_id        = z.id
            LEFT JOIN usuarios ap ON lc.aprobado_por_id = ap.id
            WHERE (:zonaId       IS NULL OR z.id            = :zonaId)
              AND (:periodoInicio IS NULL OR lc.periodo_inicio >= :periodoInicio)
              AND (:periodoFin    IS NULL OR lc.periodo_fin   <= :periodoFin)
              AND (:estado        IS NULL OR lc.estado         = :estado)
            ORDER BY lc.fecha_liquidacion DESC
            """;

        return jdbc.query(sql, params, (rs, i) -> new ReporteLiquidacionRow(
                rs.getLong("liquidacionId"),
                rs.getString("zona"),
                rs.getString("sucursal"),
                rs.getString("vendedor"),
                rs.getDate("periodo_inicio") != null ? rs.getDate("periodo_inicio").toLocalDate() : null,
                rs.getDate("periodo_fin") != null ? rs.getDate("periodo_fin").toLocalDate() : null,
                rs.getBigDecimal("montoEsperado"),
                rs.getBigDecimal("montoDepositado"),
                rs.getBigDecimal("diferencia"),
                safeEnum(EstadoLiquidacion.class, rs.getString("estado")),
                rs.getString("aprobadoPor"),
                toInstant(rs.getTimestamp("fecha_liquidacion")),
                toInstant(rs.getTimestamp("fecha_aprobacion"))
        ));
    }

    // ─────────────────────── HELPERS ─────────────────────────────────────────

    private Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }

    private <E extends Enum<E>> E safeEnum(Class<E> clazz, String value) {
        if (value == null || value.isBlank()) return null;
        try { return Enum.valueOf(clazz, value); }
        catch (IllegalArgumentException e) { return null; }
    }
}
