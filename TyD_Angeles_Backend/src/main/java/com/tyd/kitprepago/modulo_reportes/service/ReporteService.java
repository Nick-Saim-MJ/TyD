package com.tyd.kitprepago.modulo_reportes.service;

import com.tyd.kitprepago.modulo_reportes.dto.response.*;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion;
import com.tyd.kitprepago.shared.export.ColumnDefinition;
import com.tyd.kitprepago.shared.export.ExcelExportService;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteQueryRepository queryRepo;
    private final ExcelExportService excelService;
    private final ZonaContextHolder zonaCtx;

    // ─────────────────────── REPORTE DE VENTAS ───────────────────────────────

    /**
     * El reporte principal del contador.
     * ZonaContextHolder aplica el filtro automático de zona.
     * Si el caller pasa Accept: application/vnd.ms-excel → generar Excel.
     */
    @Transactional(readOnly = true)
    public List<ReporteVentaRow> reporteVentas(Long zonaId, Long sucursalId,
                                                String mes, Long vendedorId,
                                                TipoCliente tipoCliente) {
        Long zonaFiltro = zonaId != null ? zonaId
                : zonaCtx.getZonaIdFiltro().orElse(null);

        Instant desde = null, hasta = null;
        if (mes != null) {
            YearMonth ym = YearMonth.parse(mes);
            desde = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            hasta = ym.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        }

        return queryRepo.ventasParaReporte(zonaFiltro, sucursalId,
                vendedorId, tipoCliente, desde, hasta);
    }

    @Transactional(readOnly = true)
    public Page<ReporteVentaRow> reporteVentasPaginado(Long zonaId, Long sucursalId,
                                                       String mes, Long vendedorId,
                                                       TipoCliente tipoCliente,
                                                       int page, int size) {
        List<ReporteVentaRow> todos = reporteVentas(zonaId, sucursalId, mes, vendedorId, tipoCliente);

        int total = todos.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<ReporteVentaRow> pagina = fromIndex < total
                ? todos.subList(fromIndex, toIndex)
                : Collections.emptyList();

        return new PageImpl<>(pagina, PageRequest.of(page, size), total);
    }

    public byte[] reporteVentasExcel(Long zonaId, Long sucursalId,
                                      String mes, Long vendedorId,
                                      TipoCliente tipoCliente) {
        List<ReporteVentaRow> datos = reporteVentas(zonaId, sucursalId, mes, vendedorId, tipoCliente);

        List<ColumnDefinition<ReporteVentaRow>> columnas = List.of(
            new ColumnDefinition<>("N° Venta",        r -> r.ventaId()),
            new ColumnDefinition<>("Zona",             r -> r.zona()),
            new ColumnDefinition<>("Sucursal",         r -> r.sucursal()),
            new ColumnDefinition<>("Serie Maestro",    r -> r.serieMaestro()),
            new ColumnDefinition<>("SIM",              r -> r.serieSim()),
            new ColumnDefinition<>("Producto",         r -> r.producto()),
            new ColumnDefinition<>("DNI Cliente",      r -> r.clienteDni()),
            new ColumnDefinition<>("Cliente",          r -> r.clienteNombre()),
            new ColumnDefinition<>("Tipo",             r -> r.clienteTipo() != null ? r.clienteTipo().name() : ""),
            new ColumnDefinition<>("Vendedor",         r -> r.vendedor()),
            new ColumnDefinition<>("Monto Venta",      r -> r.monto()),
            new ColumnDefinition<>("Condición",        r -> r.condicion() != null ? r.condicion().name() : ""),
            new ColumnDefinition<>("Método Pago",      r -> r.metodoPago()),
            new ColumnDefinition<>("Estado",           r -> r.estado() != null ? r.estado().name() : ""),
            new ColumnDefinition<>("Monto Liquidado",  r -> r.montoLiquidado()),
            new ColumnDefinition<>("Fecha Venta",      r -> r.fechaVenta())
        );

        String titulo = "Ventas" + (mes != null ? " - " + mes : "");
        return excelService.exportar(titulo, datos, columnas);
    }

    // ─────────────────────── SNAPSHOT DE STOCK ───────────────────────────────

    @Transactional(readOnly = true)
    public List<ReporteStockRow> reporteStock(Long zonaId) {
        Long zonaFiltro = zonaId != null ? zonaId
                : zonaCtx.getZonaIdFiltro().orElse(null);
        return queryRepo.stockPorSucursal(zonaFiltro);
    }

    public byte[] reporteStockExcel(Long zonaId) {
        List<ReporteStockRow> datos = reporteStock(zonaId);
        List<ColumnDefinition<ReporteStockRow>> columnas = List.of(
            new ColumnDefinition<>("Zona",         ReporteStockRow::zona),
            new ColumnDefinition<>("Sucursal",     ReporteStockRow::sucursal),
            new ColumnDefinition<>("Tipo",         ReporteStockRow::tipoSucursal),
            new ColumnDefinition<>("Producto",     ReporteStockRow::producto),
            new ColumnDefinition<>("Disponibles",  ReporteStockRow::disponibles),
            new ColumnDefinition<>("En Tránsito",  ReporteStockRow::enTransito),
            new ColumnDefinition<>("Defectuosos",  ReporteStockRow::defectuosos),
            new ColumnDefinition<>("Vendidos",     ReporteStockRow::vendidos),
            new ColumnDefinition<>("Total",        ReporteStockRow::totalKits)
        );
        return excelService.exportar("Stock Actual - " + Instant.now().toString().substring(0, 10),
                datos, columnas);
    }

    // ─────────────────────── ACTIVACIONES PENDIENTES ─────────────────────────

    @Transactional(readOnly = true)
    public List<ReporteActivacionPendienteRow> reporteActivacionesPendientes(Long zonaId) {
        Long zonaFiltro = zonaId != null ? zonaId
                : zonaCtx.getZonaIdFiltro().orElse(null);
        List<ReporteActivacionPendienteRow> rows = queryRepo.activacionesPendientes(zonaFiltro);

        // Calcular días sin activar
        Instant ahora = Instant.now();
        return rows.stream()
                .map(r -> new ReporteActivacionPendienteRow(
                        r.ventaId(), r.activacionId(),
                        r.zona(), r.sucursal(),
                        r.serieSim(), r.serieMaestro(), r.producto(),
                        r.clienteDni(), r.clienteNombre(), r.vendedor(),
                        r.montoVenta(), r.montoRecargaInicial(),
                        r.fechaVenta(),
                        r.fechaVenta() != null
                                ? ChronoUnit.DAYS.between(r.fechaVenta(), ahora)
                                : 0L
                )).toList();
    }

    public byte[] reporteActivacionesPendientesExcel(Long zonaId) {
        List<ReporteActivacionPendienteRow> datos = reporteActivacionesPendientes(zonaId);
        List<ColumnDefinition<ReporteActivacionPendienteRow>> columnas = List.of(
            new ColumnDefinition<>("N° Venta",       ReporteActivacionPendienteRow::ventaId),
            new ColumnDefinition<>("Zona",            ReporteActivacionPendienteRow::zona),
            new ColumnDefinition<>("Sucursal",        ReporteActivacionPendienteRow::sucursal),
            new ColumnDefinition<>("SIM",             ReporteActivacionPendienteRow::serieSim),
            new ColumnDefinition<>("Serie Maestro",   ReporteActivacionPendienteRow::serieMaestro),
            new ColumnDefinition<>("DNI",             ReporteActivacionPendienteRow::clienteDni),
            new ColumnDefinition<>("Cliente",         ReporteActivacionPendienteRow::clienteNombre),
            new ColumnDefinition<>("Vendedor",        ReporteActivacionPendienteRow::vendedor),
            new ColumnDefinition<>("Monto Venta",     ReporteActivacionPendienteRow::montoVenta),
            new ColumnDefinition<>("Recarga Inicial", ReporteActivacionPendienteRow::montoRecargaInicial),
            new ColumnDefinition<>("Fecha Venta",     ReporteActivacionPendienteRow::fechaVenta),
            new ColumnDefinition<>("Días Sin Activar",ReporteActivacionPendienteRow::diasSinActivar)
        );
        return excelService.exportar("Activaciones Pendientes", datos, columnas);
    }

    // ─────────────────────── REPORTE DE LIQUIDACIONES ────────────────────────

    @Transactional(readOnly = true)
    public List<ReporteLiquidacionRow> reporteLiquidaciones(Long zonaId,
                                                              String periodoInicio,
                                                              String periodoFin,
                                                              EstadoLiquidacion estado) {
        Long zonaFiltro = zonaId != null ? zonaId
                : zonaCtx.getZonaIdFiltro().orElse(null);
        return queryRepo.liquidacionesParaReporte(zonaFiltro, periodoInicio, periodoFin, estado);
    }

    public byte[] reporteLiquidacionesExcel(Long zonaId, String periodoInicio,
                                             String periodoFin, EstadoLiquidacion estado) {
        List<ReporteLiquidacionRow> datos =
                reporteLiquidaciones(zonaId, periodoInicio, periodoFin, estado);
        List<ColumnDefinition<ReporteLiquidacionRow>> columnas = List.of(
            new ColumnDefinition<>("N° Liquidación",  ReporteLiquidacionRow::liquidacionId),
            new ColumnDefinition<>("Zona",             ReporteLiquidacionRow::zona),
            new ColumnDefinition<>("Sucursal",         ReporteLiquidacionRow::sucursal),
            new ColumnDefinition<>("Vendedor",         ReporteLiquidacionRow::vendedor),
            new ColumnDefinition<>("Periodo Inicio",   ReporteLiquidacionRow::periodoInicio),
            new ColumnDefinition<>("Periodo Fin",      ReporteLiquidacionRow::periodoFin),
            new ColumnDefinition<>("Monto Esperado",   ReporteLiquidacionRow::montoEsperado),
            new ColumnDefinition<>("Monto Depositado", ReporteLiquidacionRow::montoDepositado),
            new ColumnDefinition<>("Diferencia",       ReporteLiquidacionRow::diferencia),
            new ColumnDefinition<>("Estado",           r -> r.estado() != null ? r.estado().name() : ""),
            new ColumnDefinition<>("Aprobado Por",     ReporteLiquidacionRow::aprobadoPor),
            new ColumnDefinition<>("F. Liquidación",   ReporteLiquidacionRow::fechaLiquidacion),
            new ColumnDefinition<>("F. Aprobación",    ReporteLiquidacionRow::fechaAprobacion)
        );
        return excelService.exportar("Liquidaciones", datos, columnas);
    }
}
