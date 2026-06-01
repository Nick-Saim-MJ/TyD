import { inject } from '@angular/core';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, EMPTY } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { InventarioApiService } from './api/inventario.api';
import { ToastService } from '../../shared/services/toast.service';
import {
  FiltroLotes, GrupoEstado, ItemKitResponse,
  LoteResponse, agruparPorEstado
} from './models/inventario.model';

// ── Estado ────────────────────────────────────────────────────────────────────

interface InventarioState {
  // Lista de lotes
  lotes:          LoteResponse[];
  cargandoLotes:  boolean;
  filtroLotes:    FiltroLotes;

  // Detalle de un lote
  loteActual:     LoteResponse | null;
  kitsLote:       ItemKitResponse[];
  cargandoDetalle: boolean;

  // Búsqueda por boucher
  terminoBoucher:       string;
  kitsBoucher:          ItemKitResponse[];
  gruposBoucher:        GrupoEstado[];
  cargandoBoucher:      boolean;
  errorBoucher:         string | null;
  boucherBuscado:       string | null;

  // Error global
  error: string | null;
}

const initialState: InventarioState = {
  lotes:           [],
  cargandoLotes:   false,
  filtroLotes:     { zonaId: null, periodo: null },
  loteActual:      null,
  kitsLote:        [],
  cargandoDetalle: false,
  terminoBoucher:  '',
  kitsBoucher:     [],
  gruposBoucher:   [],
  cargandoBoucher: false,
  errorBoucher:    null,
  boucherBuscado:  null,
  error:           null,
};

// ── Store ─────────────────────────────────────────────────────────────────────

export const InventarioStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withMethods((store,
    api      = inject(InventarioApiService),
    toastSvc = inject(ToastService),
  ) => ({

    // ── Lotes ───────────────────────────────────────────────────────────────

    cargarLotes: rxMethod<FiltroLotes>(
      pipe(
        tap(filtro => patchState(store, {
          cargandoLotes: true, error: null, filtroLotes: filtro
        })),
        switchMap(filtro =>
          api.getLotes(filtro.zonaId, filtro.periodo).pipe(
            tap({
              next:  lotes  => patchState(store, { lotes, cargandoLotes: false }),
              error: (err: HttpErrorResponse) => {
                patchState(store, { cargandoLotes: false, error: err.error?.mensaje ?? 'Error al cargar lotes' });
                toastSvc.error('Error', 'No se pudo cargar la lista de lotes');
              }
            }),
            catchError(() => EMPTY)
          )
        )
      )
    ),

    // ── Detalle de lote ─────────────────────────────────────────────────────

    cargarDetalleLote: rxMethod<number>(
      pipe(
        tap(() => patchState(store, { cargandoDetalle: true, kitsLote: [] })),
        switchMap(loteId =>
          api.getItemsDeLote(loteId).pipe(
            tap({
              next: kitsLote => {
                // El loteActual se obtiene de la lista ya cargada
                const loteActual = store.lotes().find(l => l.id === loteId) ?? null;
                patchState(store, { kitsLote, loteActual, cargandoDetalle: false });
              },
              error: (err: HttpErrorResponse) => {
                patchState(store, { cargandoDetalle: false });
                toastSvc.error('Error', err.error?.mensaje ?? 'No se pudo cargar el detalle');
              }
            }),
            catchError(() => EMPTY)
          )
        )
      )
    ),

    // ── Búsqueda por boucher ────────────────────────────────────────────────

    buscarPorBoucher: rxMethod<string>(
      pipe(
        tap(termino => patchState(store, {
          cargandoBoucher: true,
          errorBoucher:    null,
          terminoBoucher:  termino,
          kitsBoucher:     [],
          gruposBoucher:   [],
        })),
        switchMap(termino =>
          api.buscarPorBoucher(termino.trim()).pipe(
            tap({
              next: kitsBoucher => {
                patchState(store, {
                  kitsBoucher,
                  gruposBoucher:   agruparPorEstado(kitsBoucher),
                  cargandoBoucher: false,
                  boucherBuscado:  termino.trim(),
                  errorBoucher:    null,
                });
              },
              error: (err: HttpErrorResponse) => {
                const msg = err.status === 404
                  ? `No se encontraron kits con N° Operación: ${termino}`
                  : (err.error?.mensaje ?? 'Error en la búsqueda');
                patchState(store, {
                  cargandoBoucher: false,
                  errorBoucher:    msg,
                  kitsBoucher:     [],
                  gruposBoucher:   [],
                });
              }
            }),
            catchError(() => EMPTY)
          )
        )
      )
    ),

    // ── Exportar Excel ──────────────────────────────────────────────────────

    exportarExcel(zonaId?: number | null, periodo?: string | null): void {
      api.exportarLotes(zonaId, periodo).subscribe({
        next: (blob) => {
          const url  = URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href  = url;
          link.download = `lotes${periodo ? '-' + periodo : ''}.xlsx`;
          link.click();
          URL.revokeObjectURL(url);
          toastSvc.success('Excel generado', 'El archivo se descargó correctamente');
        },
        error: () => toastSvc.error('Error', 'No se pudo generar el Excel'),
      });
    },

    // ── Utilidades ──────────────────────────────────────────────────────────

    setFiltro(filtro: Partial<FiltroLotes>): void {
      patchState(store, {
        filtroLotes: { ...store.filtroLotes(), ...filtro }
      });
    },

    limpiarBoucher(): void {
      patchState(store, {
        terminoBoucher: '', kitsBoucher: [],
        gruposBoucher: [], boucherBuscado: null, errorBoucher: null,
      });
    },

    limpiarDetalle(): void {
      patchState(store, { loteActual: null, kitsLote: [] });
    },

  }))
);
