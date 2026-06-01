package com.tyd.kitprepago.modulo_reportes.controller;

import com.tyd.kitprepago.modulo_reportes.service.ReporteService;
import com.tyd.kitprepago.modulo_reportes.dto.response.*;
import com.tyd.kitprepago.modulo_reportes.service.*;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','CONTADOR')")
class ReporteController {

    private final ReporteService reporteService;

    private static final String EXCEL_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * GET /api/reportes/ventas
     * El reporte principal del contador.
     *
     * JSON (por defecto):
     *   Accept: application/json
     *
     * Excel (cuando el frontend envía el header correcto):
     *   Accept: application/vnd.ms-excel
     *   O bien: GET /api/reportes/ventas?formato=excel
     *
     * Filtros: ?zonaId=&sucursalId=&mes=2025-01&vendedorId=&tipo=PDV|GENERAL
     * Sin filtros devuelve todo (ZonaContextHolder limita por rol).
     */
    @GetMapping("/ventas")
    public ResponseEntity<?> reporteVentas(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) TipoCliente tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "false") boolean excel,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept) {

        boolean generarExcel = excel || (accept != null && accept.contains("vnd.ms-excel"));

        if (generarExcel) {
            byte[] bytes = reporteService.reporteVentasExcel(zonaId, sucursalId, mes, vendedorId, tipo);
            return excelResponse(bytes, "ventas-" + (mes != null ? mes : LocalDate.now()) + ".xlsx");
        }

        // ✅ Ahora devuelve respuesta paginada
        return ResponseEntity.ok(
                reporteService.reporteVentasPaginado(zonaId, sucursalId, mes, vendedorId, tipo, page, size));
    }

    /**
     * GET /api/reportes/ventas/exportar
     * Endpoint separado para exportar sin paginación
     */
    @GetMapping("/ventas/exportar")
    public ResponseEntity<List<ReporteVentaRow>> reporteVentasExportar(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) TipoCliente tipo) {

        return ResponseEntity.ok(
                reporteService.reporteVentas(zonaId, sucursalId, mes, vendedorId, tipo));
    }

    /**
     * GET /api/reportes/stock
     * Snapshot del inventario actual por sucursal y zona.
     * Datos en tiempo real — no usa kardex_mensual.
     */
    @GetMapping("/stock")
    public ResponseEntity<?> reporteStock(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false, defaultValue = "false") boolean excel) {
        if (excel) {
            return excelResponse(reporteService.reporteStockExcel(zonaId),
                    "stock-" + LocalDate.now() + ".xlsx");
        }
        return ResponseEntity.ok(reporteService.reporteStock(zonaId));
    }

    /**
     * GET /api/reportes/activaciones-pendientes
     * Todos los kits vendidos sin activar, agrupados por zona.
     * Incluye días sin activar para priorizar seguimiento.
     */
    @GetMapping("/activaciones-pendientes")
    public ResponseEntity<?> reporteActivacionesPendientes(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false, defaultValue = "false") boolean excel) {
        if (excel) {
            return excelResponse(reporteService.reporteActivacionesPendientesExcel(zonaId),
                    "activaciones-pendientes-" + LocalDate.now() + ".xlsx");
        }
        return ResponseEntity.ok(reporteService.reporteActivacionesPendientes(zonaId));
    }

    /**
     * GET /api/reportes/liquidaciones
     * Resumen de liquidaciones con diferencias.
     * El contador usa esto para detectar faltantes (diferencia negativa).
     * Filtros: ?zonaId=&periodoInicio=2025-01&periodoFin=2025-01&estado=OBSERVADO
     */
    @GetMapping("/liquidaciones")
    public ResponseEntity<?> reporteLiquidaciones(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) String periodoInicio,
            @RequestParam(required = false) String periodoFin,
            @RequestParam(required = false) EstadoLiquidacion estado,
            @RequestParam(required = false, defaultValue = "false") boolean excel) {
        if (excel) {
            return excelResponse(
                    reporteService.reporteLiquidacionesExcel(zonaId, periodoInicio, periodoFin, estado),
                    "liquidaciones.xlsx");
        }
        return ResponseEntity.ok(
                reporteService.reporteLiquidaciones(zonaId, periodoInicio, periodoFin, estado));
    }

    // ── Helper para respuestas Excel ─────────────────────────────────────────

    private ResponseEntity<byte[]> excelResponse(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(EXCEL_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(bytes);
    }
}