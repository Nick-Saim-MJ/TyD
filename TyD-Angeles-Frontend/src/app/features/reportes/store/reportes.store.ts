import { signalStore, withState, withMethods, withComputed, patchState } from '@ngrx/signals';
import { inject } from '@angular/core';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap } from 'rxjs';
import { computed } from '@angular/core';

import { ReporteHttpService } from '../services/reporte-http.service';
import {
  FiltrosVentas, FiltrosKardex,
  VentaReporte, KardexResumen,
  ZonaOption, SucursalOption, VendedorOption,
  PagedResponse
} from '../models/reporte.models';

// ─── Estado ───────────────────────────────────────────────────────────────

interface ReportesState {
  // Catálogos
  zonas:      ZonaOption[];
  sucursales: SucursalOption[];
  vendedores: VendedorOption[];

  // Ventas
  ventasPage:      PagedResponse<VentaReporte> | null;
  filtrosVentas:   FiltrosVentas;
  loadingVentas:   boolean;
  exportingVentas: boolean;
  ventasParaExport: VentaReporte[];

  // Kardex
  kardexResumen:  KardexResumen[];
  filtrosKardex:  FiltrosKardex;
  loadingKardex:  boolean;
  kardexExpandido: number | null;

  // Errores
  errorVentas:  string | null;
  errorKardex:  string | null;
}

const initialState: ReportesState = {
  zonas:       [],
  sucursales:  [],
  vendedores:  [],

  ventasPage:       null,
  filtrosVentas:    { zonaId: null, sucursalId: null, vendedorId: null, mes: null, page: 0, size: 20 },
  loadingVentas:    false,
  exportingVentas:  false,
  ventasParaExport: [],

  kardexResumen:   [],
  filtrosKardex:   { sucursalId: null, modeloKit: null, periodo: null },
  loadingKardex:   false,
  kardexExpandido: null,

  errorVentas: null,
  errorKardex: null,
};

// ─── Store ────────────────────────────────────────────────────────────────

export const ReportesStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withComputed(store => ({
    sucursalesFiltradas: computed(() => {
      const zonaId = store.filtrosVentas().zonaId;
      return zonaId
        ? store.sucursales().filter(s => s.zonaId === zonaId)
        : store.sucursales();
    }),

    totalVentas: computed(() => store.ventasPage()?.totalElements ?? 0),
    paginaActual: computed(() => store.ventasPage()?.number ?? 0),
    hayResultadosKardex: computed(() => store.kardexResumen().length > 0),
    kardexCerrados: computed(() => store.kardexResumen().filter(k => k.cerrado).length),
    kardexAbiertos: computed(() => store.kardexResumen().filter(k => !k.cerrado).length),
  })),

  withMethods(store => {
    const http = inject(ReporteHttpService);

    return {
      // ── Catálogos ──────────────────────────────────────────────────────

      cargarCatalogos: rxMethod<void>(
        pipe(
          switchMap(() => http.getZonas()),
          tap(zonas => patchState(store, { zonas }))
        )
      ),

      cargarSucursales: rxMethod<number | undefined>(
        pipe(
          switchMap(zonaId => http.getSucursales(zonaId)),
          tap(sucursales => patchState(store, { sucursales }))
        )
      ),

      // ✅ CORREGIDO: Obtiene zonaId del store y llama correctamente al servicio
      cargarVendedores: rxMethod<number | undefined | null>(
        pipe(
          switchMap(sucursalId => {
            const zonaId = store.filtrosVentas().zonaId; // Obtenemos la zona seleccionada
            return http.getVendedores(zonaId, sucursalId);
          }),
          tap(vendedores => patchState(store, { vendedores }))
        )
      ),

      // ── Filtros Ventas ─────────────────────────────────────────────────

      setFiltroVentas(parcial: Partial<FiltrosVentas>) {
        const reset: Partial<FiltrosVentas> = {};
        if ('zonaId' in parcial) {
          reset.sucursalId = null;
          reset.vendedorId = null;
        }
        if ('sucursalId' in parcial) reset.vendedorId = null;

        patchState(store, {
          filtrosVentas: { ...store.filtrosVentas(), ...parcial, ...reset, page: 0 }
        });
      },

      setPage(page: number) {
        patchState(store, { filtrosVentas: { ...store.filtrosVentas(), page } });
      },

      // ── Ventas ─────────────────────────────────────────────────────────

      buscarVentas: rxMethod<void>(
        pipe(
          tap(() => patchState(store, { loadingVentas: true, errorVentas: null })),
          switchMap(() => http.getVentas(store.filtrosVentas())),
          tap({
            next: ventasPage => patchState(store, { ventasPage, loadingVentas: false }),
            error: () => patchState(store, {
              loadingVentas: false,
              errorVentas: 'Error al cargar ventas. Intente nuevamente.'
            })
          })
        )
      ),

      cargarVentasExport: rxMethod<void>(
        pipe(
          tap(() => patchState(store, { exportingVentas: true })),
          switchMap(() => {
            const { page: _p, size: _s, ...filtros } = store.filtrosVentas();
            return http.getVentasCompleto(filtros);
          }),
          tap({
            next: ventasParaExport => patchState(store, { ventasParaExport, exportingVentas: false }),
            error: () => patchState(store, { exportingVentas: false })
          })
        )
      ),

      // ── Kardex ─────────────────────────────────────────────────────────

      setFiltroKardex(parcial: Partial<FiltrosKardex>) {
        patchState(store, { filtrosKardex: { ...store.filtrosKardex(), ...parcial } });
      },

      buscarKardex: rxMethod<void>(
        pipe(
          tap(() => patchState(store, { loadingKardex: true, errorKardex: null })),
          switchMap(() => http.getKardexResumen(store.filtrosKardex())),
          tap({
            next: kardexResumen => {
              // Filtrar por modeloKit en el frontend (el backend no soporta este filtro)
              const modeloKit = store.filtrosKardex().modeloKit;
              const filtrado = modeloKit
                ? kardexResumen.filter(k => k.modeloKit === modeloKit)
                : kardexResumen;

              patchState(store, { kardexResumen: filtrado, loadingKardex: false });
            },
            error: () => patchState(store, {
              loadingKardex: false,
              errorKardex: 'Error al cargar kardex.'
            })
          })
        )
      ),

      toggleKardexExpandido(sucursalId: number) {
        patchState(store, {
          kardexExpandido: store.kardexExpandido() === sucursalId ? null : sucursalId
        });
      },

      limpiarExport() {
        patchState(store, { ventasParaExport: [] });
      },
    };
  })
);
