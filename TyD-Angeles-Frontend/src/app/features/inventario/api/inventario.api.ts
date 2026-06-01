import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ItemKitResponse, LoteResponse, ModeloKitResponse
} from '../models/inventario.model';

@Injectable({ providedIn: 'root' })
export class InventarioApiService {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}`;

  // ── Lotes ────────────────────────────────────────────────────────────────

  getLotes(zonaId?: number | null, periodo?: string | null): Observable<LoteResponse[]> {
    let params = new HttpParams();
    if (zonaId)  params = params.set('zonaId',  zonaId.toString());
    if (periodo) params = params.set('periodo', periodo);
    return this.http.get<LoteResponse[]>(`${this.base}/lotes`, { params });
  }

  getItemsDeLote(loteId: number): Observable<ItemKitResponse[]> {
    return this.http.get<ItemKitResponse[]>(`${this.base}/lotes/${loteId}/items`);
  }

  exportarLotes(zonaId?: number | null, periodo?: string | null): Observable<Blob> {
    let params = new HttpParams().set('excel', 'true');
    if (zonaId)  params = params.set('zonaId', zonaId.toString());
    if (periodo) params = params.set('periodo', periodo);
    return this.http.get(`${this.base}/lotes/export`, {
      params,
      responseType: 'blob',
    });
  }

  // ── Items Kit ────────────────────────────────────────────────────────────

  buscarPorBoucher(numeroOperacion: string): Observable<ItemKitResponse[]> {
    return this.http.get<ItemKitResponse[]>(`${this.base}/items-kit/por-boucher`, {
      params: new HttpParams().set('numeroOperacion', numeroOperacion),
    });
  }

  buscarPorSerial(serie: string): Observable<ItemKitResponse> {
    return this.http.get<ItemKitResponse>(`${this.base}/items-kit/buscar`, {
      params: new HttpParams().set('serie', serie),
    });
  }

  getKitsPorZona(zonaId: number): Observable<ItemKitResponse[]> {
    return this.http.get<ItemKitResponse[]>(`${this.base}/items-kit/zona/${zonaId}`);
  }

  // ── Modelos ──────────────────────────────────────────────────────────────

  getModelos(): Observable<ModeloKitResponse[]> {
    return this.http.get<ModeloKitResponse[]>(`${this.base}/modelos-kit`);
  }
}
