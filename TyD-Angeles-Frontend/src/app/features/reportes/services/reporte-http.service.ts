import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import {
  FiltrosVentas, FiltrosKardex,
  VentaReporte, KardexResumen, KardexDetalle,
  ZonaOption, SucursalOption, VendedorOption,
  PagedResponse
} from '../models/reporte.models';

@Injectable({ providedIn: 'root' })
export class ReporteHttpService {
  private readonly http = inject(HttpClient);
  // URL absoluta al backend (igual que el resto de servicios), no depende del proxy del dev server
  private readonly api  = environment.apiUrl;
  private readonly base = `${environment.apiUrl}/reportes`;

  // ── Catálogos ─────────────────────────────────────────────────────────────
  getZonas(): Observable<ZonaOption[]> {
    return this.http.get<ZonaOption[]>(`${this.api}/zonas`);
  }

  getSucursales(zonaId?: number | null): Observable<SucursalOption[]> {
    let params = new HttpParams();
    if (zonaId) params = params.set('zonaId', zonaId);
    return this.http.get<SucursalOption[]>(`${this.api}/sucursales`, { params });
  }

  getVendedores(zonaId?: number | null, sucursalId?: number | null): Observable<VendedorOption[]> {
    let params = new HttpParams();
    if (zonaId)     params = params.set('zonaId', zonaId);
    if (sucursalId) params = params.set('sucursalId', sucursalId);
    return this.http.get<any>(`${this.api}/usuarios/vendedores`, { params }).pipe(
      map(data => Array.isArray(data) ? data : [])
    );
  }

  // ── Ventas ────────────────────────────────────────────────────────────────
  getVentas(filtros: FiltrosVentas): Observable<PagedResponse<VentaReporte>> {
    let params = new HttpParams()
      .set('page', filtros.page)
      .set('size', filtros.size);

    if (filtros.zonaId)     params = params.set('zonaId', filtros.zonaId);
    if (filtros.sucursalId) params = params.set('sucursalId', filtros.sucursalId);
    if (filtros.vendedorId) params = params.set('vendedorId', filtros.vendedorId);
    if (filtros.mes)        params = params.set('mes', filtros.mes);

    return this.http.get<any>(`${this.base}/ventas`, { params }).pipe(
      map(response => {
        const rawItems      = Array.isArray(response) ? response : (response?.content || []);
        const totalElements = Array.isArray(response) ? rawItems.length : (response?.totalElements ?? rawItems.length);
        const totalPages    = Array.isArray(response)
          ? Math.ceil(rawItems.length / filtros.size)
          : (response?.totalPages ?? Math.ceil(rawItems.length / filtros.size));

        const content = rawItems.map(this._mapearVenta);

        return { content, totalElements, totalPages, number: filtros.page, size: filtros.size };
      })
    );
  }

  getVentasCompleto(filtros: Omit<FiltrosVentas, 'page' | 'size'>): Observable<VentaReporte[]> {
    let params = new HttpParams();
    if (filtros.zonaId)     params = params.set('zonaId', filtros.zonaId);
    if (filtros.sucursalId) params = params.set('sucursalId', filtros.sucursalId);
    if (filtros.vendedorId) params = params.set('vendedorId', filtros.vendedorId);
    if (filtros.mes)        params = params.set('mes', filtros.mes);

    return this.http.get<any>(`${this.base}/ventas`, { params }).pipe(
      map(response => {
        const rawItems = Array.isArray(response) ? response : (response?.content || []);
        return rawItems.map(this._mapearVenta);
      })
    );
  }

  // ── Kardex ────────────────────────────────────────────────────────────────

  /**
   * GET /api/kardex?sucursalId=X&periodo=2026-05
   * Mapea KardexResponse del backend a KardexResumen del frontend
   */
  getKardexResumen(filtros: FiltrosKardex): Observable<KardexResumen[]> {
    let params = new HttpParams();
    if (filtros.sucursalId) params = params.set('sucursalId', filtros.sucursalId);
    if (filtros.periodo)    params = params.set('periodo', filtros.periodo);

    return this.http.get<any[]>(`${this.api}/kardex`, { params }).pipe(
      map(data => data.map(item => this._mapearKardex(item)))  // ✅ Preserva el contexto
    );
  }

  /**
   * Detalle de movimientos — el backend NO tiene este endpoint
   * Retornamos array vacío por ahora
   */
  getKardexDetalle(sucursalId: number, modeloKit: string, periodo: string): Observable<KardexDetalle[]> {
    // El backend no tiene endpoint de detalle de movimientos individuales
    // Solo tiene resúmenes mensuales
    return new Observable<KardexDetalle[]>(subscriber => {
      subscriber.next([]);
      subscriber.complete();
    });
  }

  /**
   * POST /api/kardex/{id}/cerrar
   * El backend espera el ID del kardex, no sucursalId+modeloKit+periodo
   */
  cerrarKardex(kardexId: number): Observable<void> {
    return this.http.post<void>(`${this.api}/kardex/${kardexId}/cerrar`, {});
  }

  // ── Mappers ───────────────────────────────────────────────────────────────

  private _mapearVenta(row: any): VentaReporte {
    return {
      ventaId:        row.ventaId,
      zona:           row.zona,
      sucursal:       row.sucursal,
      serieMaestro:   row.serieMaestro,
      serieSim:       row.serieSim,
      producto:       row.producto,
      clienteDni:     row.clienteDni,
      clienteNombre:  row.clienteNombre,
      clienteTipo:    row.clienteTipo,
      vendedor:       row.vendedor,
      monto:          row.monto,
      condicion:      row.condicion,
      metodoPago:     row.metodoPago,
      estado:         row.estado,
      montoLiquidado: row.montoLiquidado,
      fechaVenta:     row.fechaVenta
    };
  }

  private _mapearKardex(row: any): KardexResumen {
    // Mapear productoNombre a modeloKit
    const modeloKit = this._extraerModeloKit(row.productoNombre);

    return {
      id:             row.id,
      sucursalId:     row.sucursalId,
      sucursalNombre: row.sucursalNombre,
      zonaNombre:     row.zonaNombre,
      modeloKit:      modeloKit,
      periodo:        row.periodo,
      stockInicio:    row.stockInicio ?? 0,
      entradas:       row.totalIngresos ?? 0,
      salidas:        row.totalSalidas ?? 0,
      stockFin:       row.stockFin ?? 0,
      cerrado:        row.cerrado ?? false,
      fechaCierre:    row.updatedAt  // El backend no tiene fechaCierre específica, usamos updatedAt
    };
  }

  private _extraerModeloKit(productoNombre: string): 'LH100' | 'LH300' {
    // Inferir modelo del nombre del producto
    // "Kit Prepago DirecTV Basico" → LH100
    // "Kit Prepago DirecTV Plus" → LH300
    if (!productoNombre) return 'LH100';

    const nombre = productoNombre.toLowerCase();
    if (nombre.includes('plus') || nombre.includes('lh300')) {
      return 'LH300';
    }
    return 'LH100';
  }
}
