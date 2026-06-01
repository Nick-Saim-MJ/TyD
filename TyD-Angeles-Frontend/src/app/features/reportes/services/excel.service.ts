import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx-js-style';
import { VentaReporte, KardexResumen } from '../models/reporte.models';

@Injectable({ providedIn: 'root' })
export class ExcelService {

  // ── Paleta de colores ────────────────────────────────────────────────────
  private readonly colores = {
    headerBg:    '1A3A5C', // Azul corporativo oscuro
    headerText:  'FFFFFF',

    colHeadBg:   '475569', // Gris pizarra
    colHeadText: 'FFFFFF',

    rowEvenBg:   'EFF6FF', // Celeste muy suave
    rowOddBg:    'FFFFFF', // Blanco
    rowText:     '0F172A',

    borde:       'CBD5E1', // Gris claro

    exitoBg:     'DCFCE7',
    exitoText:   '166534',
    errorBg:     'FEE2E2',
    errorText:   '991B1B',
    alertaBg:    'FEF3C7',
    alertaText:  '92400E',

    serialBg:    'DBEAFE',
    serialText:  '1E40AF',
  };

  // ── Ventas (formato profesional con header y estilos) ─────────────────
  exportarVentas(ventas: VentaReporte[], filtroLabel: string): void {
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([]);

    // ── Estructura de datos ──────────────────────────────────────────────
    const headers = [
      'N° Venta', 'Zona', 'Sucursal', 'Serie Maestro', 'SIM',
      'Producto', 'DNI Cliente', 'Cliente', 'Tipo', 'Vendedor',
      'Monto Venta', 'Condición', 'Método Pago', 'Estado',
      'Monto Liquidado', 'Fecha Venta'
    ];

    const filas = ventas.map(v => [
      v.ventaId,
      v.zona,
      v.sucursal,
      v.serieMaestro,
      v.serieSim,
      v.producto,
      v.clienteDni,
      v.clienteNombre,
      v.clienteTipo ?? '—',
      v.vendedor,
      v.monto,
      v.condicion ?? '—',
      v.metodoPago ?? '—',
      v.estado ?? '—',
      v.montoLiquidado ?? 0,
      this._formatearFecha(v.fechaVenta),
    ]);

    // ── Construir matriz AOA (Array of Arrays) ─────────────────────────
    const aoa: any[][] = [];

    // Fila 1: Título principal "VENTAS"
    aoa.push(['REPORTE DE VENTAS']);
    // Fila 2: Subtítulo con filtros
    aoa.push(['Filtros: ' + filtroLabel]);
    // Fila 3: Fecha de generación
    aoa.push(['Generado: ' + new Date().toLocaleString('es-PE') + '  |  Total: ' + ventas.length + ' registros']);
    // Fila 4: Vacía
    aoa.push([]);
    // Fila 5: Cabeceras
    aoa.push(headers);
    // Filas 6+: Datos
    filas.forEach(f => aoa.push(f));

    // Cargar al worksheet
    const wsFinal = XLSX.utils.aoa_to_sheet(aoa);

    // ── Merge del título (A1:P1) ───────────────────────────────────────
    wsFinal['!merges'] = [
      { s: { r: 0, c: 0 }, e: { r: 0, c: 15 } }, // Título
      { s: { r: 1, c: 0 }, e: { r: 1, c: 15 } }, // Subtítulo
      { s: { r: 2, c: 0 }, e: { r: 2, c: 15 } }, // Meta
    ];

    // ── Anchos de columna ──────────────────────────────────────────────
    wsFinal['!cols'] = [
      { wch: 10 }, // N° Venta
      { wch: 18 }, // Zona
      { wch: 22 }, // Sucursal
      { wch: 18 }, // Serie Maestro
      { wch: 16 }, // SIM
      { wch: 28 }, // Producto
      { wch: 12 }, // DNI
      { wch: 28 }, // Cliente
      { wch: 10 }, // Tipo
      { wch: 24 }, // Vendedor
      { wch: 14 }, // Monto Venta
      { wch: 12 }, // Condición
      { wch: 14 }, // Método Pago
      { wch: 12 }, // Estado
      { wch: 14 }, // Monto Liquidado
      { wch: 20 }, // Fecha Venta
    ];

    // ── Altura de filas ────────────────────────────────────────────────
    wsFinal['!rows'] = [
      { hpt: 36 }, // Título
      { hpt: 18 }, // Subtítulo
      { hpt: 16 }, // Meta
      { hpt: 8 },  // Vacía
      { hpt: 28 }, // Cabeceras
    ];
    // Filas de datos
    for (let i = 5; i <= 5 + filas.length; i++) {
      wsFinal['!rows']!.push({ hpt: 22 });
    }

    // ── Aplicar estilos ────────────────────────────────────────────────
    this._aplicarEstilos(wsFinal, filas.length);

    XLSX.utils.book_append_sheet(wb, wsFinal, 'Ventas');
    XLSX.writeFile(wb, `TyG_Ventas_${this._timestamp()}.xlsx`);
  }

  // ── Aplicador de estilos completo ─────────────────────────────────────
  private _aplicarEstilos(ws: XLSX.WorkSheet, numFilasDatos: number): void {
    const range = XLSX.utils.decode_range(ws['!ref'] ?? 'A1');
    const numCols = 16;

    for (let R = range.s.r; R <= range.e.r; ++R) {
      for (let C = range.s.c; C < numCols; ++C) {
        const addr = XLSX.utils.encode_cell({ r: R, c: C });
        if (!ws[addr]) ws[addr] = { v: '', t: 's' };

        // ── FILA 0: Título principal "VENTAS" ────────────────────────
        if (R === 0) {
          ws[addr].s = {
            font: { bold: true, size: 20, color: { rgb: this.colores.headerText }, name: 'Calibri' },
            fill: { fgColor: { rgb: this.colores.headerBg } },
            alignment: { horizontal: 'center', vertical: 'center' },
            border: this._borde(this.colores.headerBg),
          };
          continue;
        }

        // ── FILA 1: Subtítulo con filtros ────────────────────────────
        if (R === 1) {
          ws[addr].s = {
            font: { italic: true, size: 11, color: { rgb: '475569' } },
            fill: { fgColor: { rgb: 'F1F5F9' } },
            alignment: { horizontal: 'left', vertical: 'center' },
            border: this._borde('E2E8F0'),
          };
          continue;
        }

        // ── FILA 2: Metadatos ────────────────────────────────────────
        if (R === 2) {
          ws[addr].s = {
            font: { size: 9, color: { rgb: '64748B' } },
            fill: { fgColor: { rgb: 'F8FAFC' } },
            alignment: { horizontal: 'left', vertical: 'center' },
            border: this._borde('E2E8F0'),
          };
          continue;
        }

        // ── FILA 3: Vacía ────────────────────────────────────────────
        if (R === 3) {
          ws[addr].s = { fill: { fgColor: { rgb: 'FFFFFF' } } };
          continue;
        }

        // ── FILA 4: Cabeceras de columna (gris) ──────────────────────
        if (R === 4) {
          ws[addr].s = {
            font: { bold: true, size: 10, color: { rgb: this.colores.colHeadText }, name: 'Calibri' },
            fill: { fgColor: { rgb: this.colores.colHeadBg } },
            alignment: { horizontal: 'center', vertical: 'center', wrapText: true },
            border: this._borde('1F2937'),
          };
          continue;
        }

        // ── FILAS 5+: Datos (alternancia celeste/blanco) ─────────────
        const filaDatos = R - 5;
        const esPar = filaDatos % 2 === 0;
        const bgColor = esPar ? this.colores.rowEvenBg : this.colores.rowOddBg;

        // Estilo base para celdas de datos
        const baseStyle = {
          font: { size: 10, color: { rgb: this.colores.rowText }, name: 'Calibri' },
          fill: { fgColor: { rgb: bgColor } },
          alignment: { horizontal: 'left', vertical: 'center' },
          border: this._borde(this.colores.borde),
        };

        // ── Estilos por columna específica ───────────────────────────
        switch (C) {
          case 0: // N° Venta
            ws[addr].s = { ...baseStyle, alignment: { horizontal: 'center', vertical: 'center' },
              font: { size: 10, color: { rgb: '64748B' }, bold: true } };
            break;

          case 3: // Serie Maestro
          case 4: // SIM
            ws[addr].s = {
              ...baseStyle,
              font: { size: 9, name: 'Courier New', color: { rgb: this.colores.serialText }, bold: C === 3 },
              fill: { fgColor: { rgb: C === 3 ? this.colores.serialBg : 'F0FDF4' } },
              alignment: { horizontal: 'center', vertical: 'center' },
            };
            break;

          case 10: // Monto Venta
          case 14: // Monto Liquidado
            ws[addr].s = {
              ...baseStyle,
              font: { size: 10, bold: true, color: { rgb: this.colores.rowText } },
              alignment: { horizontal: 'right', vertical: 'center' },
              numFmt: '"S/" #,##0.00',
            };
            break;

          case 13: // Estado
            const estado = ws[addr].v;
            let estadoBg = bgColor, estadoText = this.colores.rowText;
            if (estado === 'ACTIVA' || estado === 'ACTIVADO') {
              estadoBg = this.colores.exitoBg; estadoText = this.colores.exitoText;
            } else if (estado === 'ANULADA' || estado === 'RECHAZADO') {
              estadoBg = this.colores.errorBg; estadoText = this.colores.errorText;
            } else if (estado === 'PENDIENTE') {
              estadoBg = this.colores.alertaBg; estadoText = this.colores.alertaText;
            }
            ws[addr].s = {
              ...baseStyle,
              fill: { fgColor: { rgb: estadoBg } },
              font: { size: 9, bold: true, color: { rgb: estadoText } },
              alignment: { horizontal: 'center', vertical: 'center' },
            };
            break;

          case 15: // Fecha
            ws[addr].s = {
              ...baseStyle,
              font: { size: 9, color: { rgb: '64748B' } },
              alignment: { horizontal: 'center', vertical: 'center' },
            };
            break;

          default:
            ws[addr].s = baseStyle;
        }
      }
    }
  }

  // ── Kardex ─────────────────────────────────────────────────────────────
  // ── Kardex (formato profesional con header y estilos) ─────────────────────
  exportarKardex(kardex: KardexResumen[], periodo: string): void {
    const wb = XLSX.utils.book_new();

    // ── Estructura de datos ──────────────────────────────────────────────
    const headers = [
      'Sucursal', 'Zona', 'Modelo', 'Período',
      'Stock Inicio', 'Entradas', 'Salidas', 'Stock Final',
      'Variación', 'Estado', 'Fecha Cierre'
    ];

    const filas = kardex.map(k => {
      const variacion = (k.stockFin ?? 0) - (k.stockInicio ?? 0);
      return [
        k.sucursalNombre,
        k.zonaNombre,
        k.modeloKit,
        k.periodo,
        k.stockInicio ?? 0,
        k.entradas ?? 0,
        k.salidas ?? 0,
        k.stockFin ?? 0,
        variacion,
        k.cerrado ? 'CERRADO' : 'ABIERTO',
        k.fechaCierre ? this._formatearFecha(k.fechaCierre) : '—',
      ];
    });

    // ── Construir matriz AOA ─────────────────────────────────────────────
    const aoa: any[][] = [];

    // Fila 1: Título principal
    aoa.push(['REPORTE DE KARDEX MENSUAL']);
    // Fila 2: Subtítulo con periodo
    aoa.push([`Período: ${periodo ?? 'Todos'}`]);
    // Fila 3: Metadatos
    const cerrados = kardex.filter(k => k.cerrado).length;
    const abiertos = kardex.length - cerrados;
    aoa.push([
      `Generado: ${new Date().toLocaleString('es-PE')}  |  ` +
      `Total: ${kardex.length} registros  |  ` +
      `Cerrados: ${cerrados}  |  Abiertos: ${abiertos}`
    ]);
    // Fila 4: Vacía
    aoa.push([]);
    // Fila 5: Cabeceras
    aoa.push(headers);
    // Filas 6+: Datos
    filas.forEach(f => aoa.push(f));

    // Cargar al worksheet
    const wsFinal = XLSX.utils.aoa_to_sheet(aoa);

    // ── Merges del título (A1:K1) ───────────────────────────────────────
    const numCols = headers.length; // 11
    wsFinal['!merges'] = [
      { s: { r: 0, c: 0 }, e: { r: 0, c: numCols - 1 } }, // Título
      { s: { r: 1, c: 0 }, e: { r: 1, c: numCols - 1 } }, // Subtítulo
      { s: { r: 2, c: 0 }, e: { r: 2, c: numCols - 1 } }, // Meta
    ];

    // ── Anchos de columna ────────────────────────────────────────────────
    wsFinal['!cols'] = [
      { wch: 26 }, // Sucursal
      { wch: 18 }, // Zona
      { wch: 10 }, // Modelo
      { wch: 12 }, // Período
      { wch: 13 }, // Stock Inicio
      { wch: 12 }, // Entradas
      { wch: 12 }, // Salidas
      { wch: 13 }, // Stock Final
      { wch: 12 }, // Variación
      { wch: 12 }, // Estado
      { wch: 20 }, // Fecha Cierre
    ];

    // ── Altura de filas ──────────────────────────────────────────────────
    wsFinal['!rows'] = [
      { hpt: 36 }, // Título
      { hpt: 18 }, // Subtítulo
      { hpt: 16 }, // Meta
      { hpt: 8 },  // Vacía
      { hpt: 28 }, // Cabeceras
    ];
    for (let i = 5; i <= 5 + filas.length; i++) {
      wsFinal['!rows']!.push({ hpt: 22 });
    }

    // ── Aplicar estilos ──────────────────────────────────────────────────
    this._aplicarEstilosKardex(wsFinal, filas.length, numCols);

    XLSX.utils.book_append_sheet(wb, wsFinal, 'Kardex');
    XLSX.writeFile(wb, `TyG_Kardex_${periodo ?? 'todos'}_${this._timestamp()}.xlsx`);
  }

// ── Aplicador de estilos para Kardex ─────────────────────────────────────
  private _aplicarEstilosKardex(ws: XLSX.WorkSheet, numFilasDatos: number, numCols: number): void {
    const range = XLSX.utils.decode_range(ws['!ref'] ?? 'A1');

    for (let R = range.s.r; R <= range.e.r; ++R) {
      for (let C = range.s.c; C < numCols; ++C) {
        const addr = XLSX.utils.encode_cell({ r: R, c: C });
        if (!ws[addr]) ws[addr] = { v: '', t: 's' };

        // ── FILA 0: Título principal ────────────────────────────────────
        if (R === 0) {
          ws[addr].s = {
            font: { bold: true, size: 20, color: { rgb: this.colores.headerText }, name: 'Calibri' },
            fill: { fgColor: { rgb: this.colores.headerBg } },
            alignment: { horizontal: 'center', vertical: 'center' },
            border: this._borde(this.colores.headerBg),
          };
          continue;
        }

        // ── FILA 1: Subtítulo ───────────────────────────────────────────
        if (R === 1) {
          ws[addr].s = {
            font: { italic: true, size: 11, color: { rgb: '475569' } },
            fill: { fgColor: { rgb: 'F1F5F9' } },
            alignment: { horizontal: 'left', vertical: 'center' },
            border: this._borde('E2E8F0'),
          };
          continue;
        }

        // ── FILA 2: Metadatos ───────────────────────────────────────────
        if (R === 2) {
          ws[addr].s = {
            font: { size: 9, color: { rgb: '64748B' } },
            fill: { fgColor: { rgb: 'F8FAFC' } },
            alignment: { horizontal: 'left', vertical: 'center' },
            border: this._borde('E2E8F0'),
          };
          continue;
        }

        // ── FILA 3: Vacía ───────────────────────────────────────────────
        if (R === 3) {
          ws[addr].s = { fill: { fgColor: { rgb: 'FFFFFF' } } };
          continue;
        }

        // ── FILA 4: Cabeceras de columna (gris pizarra) ─────────────────
        if (R === 4) {
          ws[addr].s = {
            font: { bold: true, size: 10, color: { rgb: this.colores.colHeadText }, name: 'Calibri' },
            fill: { fgColor: { rgb: this.colores.colHeadBg } },
            alignment: { horizontal: 'center', vertical: 'center', wrapText: true },
            border: this._borde('1F2937'),
          };
          continue;
        }

        // ── FILAS 5+: Datos (alternancia celeste/blanco) ────────────────
        const filaDatos = R - 5;
        const esPar = filaDatos % 2 === 0;
        const bgColor = esPar ? this.colores.rowEvenBg : this.colores.rowOddBg;

        const baseStyle = {
          font: { size: 10, color: { rgb: this.colores.rowText }, name: 'Calibri' },
          fill: { fgColor: { rgb: bgColor } },
          alignment: { horizontal: 'left', vertical: 'center' },
          border: this._borde(this.colores.borde),
        };

        // ── Estilos por columna específica ─────────────────────────────
        switch (C) {
          case 0: // Sucursal
            ws[addr].s = {
              ...baseStyle,
              font: { size: 10, bold: true, color: { rgb: this.colores.rowText } },
            };
            break;

          case 1: // Zona
            ws[addr].s = {
              ...baseStyle,
              font: { size: 10, color: { rgb: '475569' } },
            };
            break;

          case 2: // Modelo (LH100 / LH300)
            ws[addr].s = {
              ...baseStyle,
              font: { size: 9, name: 'Courier New', color: { rgb: this.colores.serialText }, bold: true },
              fill: { fgColor: { rgb: this.colores.serialBg } },
              alignment: { horizontal: 'center', vertical: 'center' },
            };
            break;

          case 3: // Período (YYYY-MM)
            ws[addr].s = {
              ...baseStyle,
              font: { size: 10, name: 'Courier New', color: { rgb: '475569' }, bold: true },
              alignment: { horizontal: 'center', vertical: 'center' },
            };
            break;

          case 4: // Stock Inicio
          case 5: // Entradas
          case 6: // Salidas
          case 7: // Stock Final
            ws[addr].s = {
              ...baseStyle,
              font: { size: 10, bold: C === 7, color: { rgb: this.colores.rowText } },
              alignment: { horizontal: 'center', vertical: 'center' },
              numFmt: '#,##0',
            };
            // Entradas en verde
            if (C === 5) {
              ws[addr].s.font = { size: 10, color: { rgb: '15803D' }, bold: true };
            }
            // Salidas en rojo
            if (C === 6) {
              ws[addr].s.font = { size: 10, color: { rgb: 'B91C1C' }, bold: true };
            }
            // Stock Final destacado
            if (C === 7) {
              ws[addr].s.fill = { fgColor: { rgb: 'F1F5F9' } };
            }
            break;

          case 8: // Variación (+/-)
            const val = ws[addr].v;
            const esPositivo = typeof val === 'number' && val > 0;
            const esNegativo = typeof val === 'number' && val < 0;
            ws[addr].s = {
              ...baseStyle,
              font: {
                size: 10,
                bold: true,
                color: { rgb: esPositivo ? '15803D' : esNegativo ? 'B91C1C' : '64748B' },
              },
              alignment: { horizontal: 'center', vertical: 'center' },
              numFmt: esPositivo ? '+#,##0;-#,##0' : '#,##0',
            };
            break;

          case 9: // Estado (CERRADO / ABIERTO)
            const estado = ws[addr].v;
            let estBg = bgColor, estText = this.colores.rowText;
            if (estado === 'CERRADO') {
              estBg = 'F1F5F9';     // Gris claro
              estText = '475569';    // Gris oscuro
            } else if (estado === 'ABIERTO') {
              estBg = this.colores.exitoBg;   // Verde suave
              estText = this.colores.exitoText; // Verde oscuro
            }
            ws[addr].s = {
              ...baseStyle,
              fill: { fgColor: { rgb: estBg } },
              font: { size: 9, bold: true, color: { rgb: estText } },
              alignment: { horizontal: 'center', vertical: 'center' },
            };
            break;

          case 10: // Fecha Cierre
            ws[addr].s = {
              ...baseStyle,
              font: { size: 9, color: { rgb: '64748B' } },
              alignment: { horizontal: 'center', vertical: 'center' },
            };
            break;

          default:
            ws[addr].s = baseStyle;
        }
      }
    }
  }
  // ── Helpers ─────────────────────────────────────────────────────────────
  private _borde(color: string): any {
    const b = { style: 'thin', color: { rgb: color } };
    return { top: b, bottom: b, left: b, right: b };
  }

  private _formatearFecha(fechaISO: string): string {
    if (!fechaISO) return '—';
    const fecha = new Date(fechaISO);
    return fecha.toLocaleString('es-PE', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
  }

  private _estilizarSimple(ws: XLSX.WorkSheet, numCols: number): void {
    const range = XLSX.utils.decode_range(ws['!ref'] ?? 'A1');
    for (let col = range.s.c; col <= Math.min(range.e.c, numCols - 1); col++) {
      const addr = XLSX.utils.encode_cell({ r: 0, c: col });
      if (!ws[addr]) continue;
      ws[addr].s = {
        font: { bold: true, color: { rgb: 'FFFFFF' } },
        fill: { fgColor: { rgb: this.colores.colHeadBg } },
        alignment: { horizontal: 'center' },
      };
    }
  }

  private _timestamp(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
