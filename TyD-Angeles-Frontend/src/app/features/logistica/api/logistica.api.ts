import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ConfirmarRecepcionRequest, CrearDespachoRequest,
  DespachoDetalleResponse, DespachoResponse,
  HistorialCustodioResponse,
  SucursalOpcion
} from '../models/logistica.model';
import { ItemKitResponse } from '../../inventario/models/inventario.model';

@Injectable({ providedIn: 'root' })
export class LogisticaApiService {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  // ── Despachos ────────────────────────────────────────────────────────────

  getDespachos(): Observable<DespachoResponse[]> {
    return this.http.get<DespachoResponse[]>(`${this.base}/despachos`);
  }

  getPendientesRecepcion(): Observable<DespachoResponse[]> {
    return this.http.get<DespachoResponse[]>(
      `${this.base}/despachos/pendientes-recepcion`
    );
  }

  getDespachoDetalle(id: number): Observable<DespachoDetalleResponse> {
    return this.http.get<DespachoDetalleResponse>(`${this.base}/despachos/${id}`);
  }

  crearDespacho(req: CrearDespachoRequest): Observable<DespachoResponse> {
    return this.http.post<DespachoResponse>(`${this.base}/despachos`, req);
  }

  confirmarRecepcion(
    despachoId: number,
    req: ConfirmarRecepcionRequest
  ): Observable<{ despachoId: number; estadoFinal: string; kitsRecibidosOk: number;
                   kitsDefectuosos: number; kitsNoRecibidos: number; mensaje: string }> {
    return this.http.put(
      `${this.base}/despachos/${despachoId}/confirmar-recepcion`, req
    ) as any;
  }

  // ── Historial de recepciones propias ──────────────────────────────────────

  getMisRecepciones(): Observable<HistorialCustodioResponse[]> {
    return this.http.get<HistorialCustodioResponse[]>(`${this.base}/mis-recepciones`);
  }

  // ── Kits disponibles (para seleccionar al crear despacho) ────────────────

  buscarKitPorSerial(serie: string): Observable<ItemKitResponse> {
    return this.http.get<ItemKitResponse>(`${this.base}/items-kit/buscar`, {
      params: new HttpParams().set('serie', serie),
    });
  }

  getKitsDisponiblesSucursal(zonaId: number): Observable<ItemKitResponse[]> {
    return this.http.get<ItemKitResponse[]>(`${this.base}/items-kit/zona/${zonaId}`);
  }

  // ── Sucursales (para los selectores) ────────────────────────────────────

  getSucursales(): Observable<SucursalOpcion[]> {
    return this.http.get<SucursalOpcion[]>(`${this.base}/sucursales`);
  }

  getKitsDisponibles(sucursalId: number): Observable<ItemKitResponse[]> {
    return this.http.get<ItemKitResponse[]>(
      `${this.base}/kits-disponibles?sucursalId=${sucursalId}`
    );
  }
}
