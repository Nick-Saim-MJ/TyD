import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  AnularVentaRequest, ClienteResponse,
  FiltroVentas, RegistrarVentaRequest,
  UsuarioVendedor, VentaResponse
} from '../models/ventas.model';
import { ItemKitResponse } from '../../inventario/models/inventario.model';

@Injectable({ providedIn: 'root' })
export class VentasApiService {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  // ── Clientes ─────────────────────────────────────────────────────────────

  buscarClientes(q: string): Observable<ClienteResponse[]> {
    return this.http.get<ClienteResponse[]>(`${this.base}/clientes/buscar`, {
      params: new HttpParams().set('q', q),
    });
  }

  crearCliente(req: Partial<ClienteResponse>): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(`${this.base}/clientes`, req);
  }

  // ── Kits ─────────────────────────────────────────────────────────────────

  buscarKitPorSerial(serie: string): Observable<ItemKitResponse> {
    return this.http.get<ItemKitResponse>(`${this.base}/items-kit/buscar`, {
      params: new HttpParams().set('serie', serie),
    });
  }

  getKitsDisponiblesPorZona(zonaId: number): Observable<ItemKitResponse[]> {
    return this.http.get<ItemKitResponse[]>(`${this.base}/items-kit/zona/${zonaId}`);
  }

  // ── Vendedores ────────────────────────────────────────────────────────────

  getVendedoresPorZona(zonaId: number): Observable<UsuarioVendedor[]> {
    return this.http.get<UsuarioVendedor[]>(`${this.base}/usuarios/vendedores`, {
      params: new HttpParams().set('zonaId', zonaId.toString()),
    });
  }

  // ── Ventas ────────────────────────────────────────────────────────────────

  getVentas(filtros: FiltroVentas): Observable<VentaResponse[]> {
    let params = new HttpParams();
    if (filtros.sucursalId) params = params.set('sucursalId', filtros.sucursalId.toString());
    if (filtros.mes)        params = params.set('mes', filtros.mes);
    if (filtros.tipo)       params = params.set('tipo', filtros.tipo);
    if (filtros.vendedorId) params = params.set('vendedorId', filtros.vendedorId.toString());
    return this.http.get<VentaResponse[]>(`${this.base}/ventas`, { params });
  }

  registrarVenta(req: RegistrarVentaRequest): Observable<{
    ventaId: number; activacionId: number;
    serieMaestro: string; serieSim: string;
    clienteNombreCompleto: string; mensaje: string; fechaVenta: string;
  }> {
    return this.http.post<any>(`${this.base}/ventas`, req);
  }

  anularVenta(id: number, req: AnularVentaRequest): Observable<VentaResponse> {
    return this.http.post<VentaResponse>(`${this.base}/ventas/${id}/anular`, req);
  }
}
