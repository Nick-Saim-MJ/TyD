package com.tyd.kitprepago.shared.export;

import com.tyd.kitprepago.shared.exception.ExcelGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio genérico de exportación a Excel (.xlsx).
 *
 * Uso desde cualquier módulo:
 *   List<ColumnDefinition<VentaDto>> columnas = List.of(
 *       new ColumnDefinition<>("Serie Maestro", VentaDto::getSerieMaestro),
 *       new ColumnDefinition<>("Monto",         VentaDto::getMonto)
 *   );
 *   byte[] archivo = excelExportService.exportar("Ventas Enero 2025", datos, columnas);
 *
 * En el controller:
 *   return ResponseEntity.ok()
 *       .header("Content-Disposition", "attachment; filename=ventas.xlsx")
 *       .contentType(MediaType.APPLICATION_OCTET_STREAM)
 *       .body(archivo);
 */
@Service
@Slf4j
public class ExcelExportService {

    private static final int FILA_DATOS_INICIO = 2;

    public <T> byte[] exportar(String titulo, List<T> datos, List<ColumnDefinition<T>> columnas) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(titulo.length() > 31 ? titulo.substring(0, 31) : titulo);

            CellStyle csTitulo  = estiloTitulo(wb);
            CellStyle csHeader  = estiloHeader(wb);
            CellStyle csNormal  = estiloFila(wb, false);
            CellStyle csAlt     = estiloFila(wb, true);
            CellStyle csMonedaNormal = estiloMoneda(wb, false);
            CellStyle csMonedaAlt    = estiloMoneda(wb, true);
            CellStyle csFechaNormal  = estiloFecha(wb, false);
            CellStyle csFechaAlt     = estiloFecha(wb, true);
            CellStyle csFechaHoraNormal = estiloFechaHora(wb, false);
            CellStyle csFechaHoraAlt    = estiloFechaHora(wb, true);

            // Fila 0 — Título
            Row rTitulo = sheet.createRow(0);
            rTitulo.setHeightInPoints(22);
            Cell cTitulo = rTitulo.createCell(0);
            cTitulo.setCellValue(titulo);
            cTitulo.setCellStyle(csTitulo);
            if (columnas.size() > 1) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnas.size() - 1));
            }

            // Fila 1 — Headers
            Row rHeaders = sheet.createRow(1);
            for (int i = 0; i < columnas.size(); i++) {
                Cell c = rHeaders.createCell(i);
                c.setCellValue(columnas.get(i).getNombre());
                c.setCellStyle(csHeader);
            }

            // Filas de datos
            for (int ri = 0; ri < datos.size(); ri++) {
                T item = datos.get(ri);
                Row fila = sheet.createRow(FILA_DATOS_INICIO + ri);
                CellStyle base = (ri % 2 == 0) ? csNormal : csAlt;
                CellStyle moneda = (ri % 2 == 0) ? csMonedaNormal : csMonedaAlt;
                CellStyle fecha = (ri % 2 == 0) ? csFechaNormal : csFechaAlt;
                CellStyle fechaHora = (ri % 2 == 0) ? csFechaHoraNormal : csFechaHoraAlt;

                for (int ci = 0; ci < columnas.size(); ci++) {
                    Cell c = fila.createCell(ci);
                    Object val = columnas.get(ci).getExtractor().apply(item);
                    if (val == null) {
                        c.setCellValue(""); c.setCellStyle(base);
                    } else if (val instanceof BigDecimal || val instanceof Double || val instanceof Float) {
                        c.setCellValue(((Number) val).doubleValue()); c.setCellStyle(moneda);
                    } else if (val instanceof Number n) {
                        c.setCellValue(n.doubleValue()); c.setCellStyle(base);
                    } else if (val instanceof Boolean b) {
                        c.setCellValue(b ? "SÍ" : "NO"); c.setCellStyle(base);
                    } else if (val instanceof Instant inst) {
                        c.setCellValue(java.util.Date.from(inst)); c.setCellStyle(fechaHora);
                    } else if (val instanceof LocalDate ld) {
                        c.setCellValue(java.util.Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())); c.setCellStyle(fecha);
                    } else if (val instanceof LocalDateTime ldt) {
                        c.setCellValue(java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant())); c.setCellStyle(fechaHora);
                    } else if (val instanceof java.util.Date d) {
                        c.setCellValue(d); c.setCellStyle(fechaHora);
                    } else {
                        c.setCellValue(val.toString()); c.setCellStyle(base);
                    }
                }
            }

            // Autoajuste + freeze headers
            for (int i = 0; i < columnas.size(); i++) {
                sheet.autoSizeColumn(i);
                int w = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(Math.max(w, 3000), 15000));
            }
            sheet.createFreezePane(0, FILA_DATOS_INICIO);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new ExcelGenerationException("Error generando Excel: " + titulo, ex);
        }
    }

    private CellStyle estiloTitulo(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short) 13);
        f.setColor(IndexedColors.WHITE.getIndex()); s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle estiloHeader(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.MEDIUM);
        return s;
    }

    private CellStyle estiloFila(Workbook wb, boolean alt) {
        CellStyle s = wb.createCellStyle();
        if (alt) {
            s.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        s.setBorderBottom(BorderStyle.THIN);
        s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return s;
    }

    private CellStyle estiloMoneda(Workbook wb, boolean alt) {
        CellStyle s = estiloFila(wb, alt);
        s.setDataFormat(wb.createDataFormat().getFormat("\"S/ \"#,##0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private CellStyle estiloFecha(Workbook wb, boolean alt) {
        CellStyle s = estiloFila(wb, alt);
        s.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy"));
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle estiloFechaHora(Workbook wb, boolean alt) {
        CellStyle s = estiloFila(wb, alt);
        s.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }
}
