import { inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { EMPTY, catchError, debounceTime, distinctUntilChanged, pipe, switchMap, tap } from 'rxjs';
import { VentasApiService } from './api/ventas.api';
import { ToastService } from '../../shared/services/toast.service';
import {
  AnularVentaRequest, ClienteResponse,
  EstadoEscaneoKit, FiltroVentas,
  KitValidado, RegistrarVentaRequest,
  UsuarioVendedor, VentaResponse
} from './models/ventas.model';

// ── Estado ────────────────────────────────────────────────────────────────────

interface VentasState {
  // Historial
  ventas:         VentaResponse[];
  cargandoVentas: boolean;
  filtros:        FiltroVentas;

  // Escaneo del kit
  estadoEscaneo:  EstadoEscaneoKit;
  kitValidado:    KitValidado | null;
  errorEscaneo:   string | null;

  // Búsqueda de clientes (autocomplete)
  clientesBusqueda:   ClienteResponse[];
  buscandoClientes:   boolean;
  clienteSeleccionado: ClienteResponse | null;

  // Vendedores por zona (selector del formulario)
  vendedores: UsuarioVendedor[];

  // Estado del registro
  guardando:  boolean;
  ventaExito: { ventaId: number; serieMaestro: string; cliente: string } | null;
  error:      string | null;
}

const initialState: VentasState = {
  ventas:              [],
  cargandoVentas:      false,
  filtros:             {},
  estadoEscaneo:       'IDLE',
  kitValidado:         null,
  errorEscaneo:        null,
  clientesBusqueda:    [],
  buscandoClientes:    false,
  clienteSeleccionado: null,
  vendedores:          [],
  guardando:           false,
  ventaExito:          null,
  error:               null,
};

// ── Store ─────────────────────────────────────────────────────────────────────

export const VentasStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withMethods((store,
    api      = inject(VentasApiService),
    toastSvc = inject(ToastService),
    router   = inject(Router),
  ) => ({

    // ── Historial de ventas ─────────────────────────────────────────────────

    cargarVentas: rxMethod<FiltroVentas>(
      pipe(
        tap(f => patchState(store, { cargandoVentas: true, filtros: f, error: null })),
        switchMap(f =>
          api.getVentas(f).pipe(
            tap({
              next:  ventas => patchState(store, { ventas, cargandoVentas: false }),
              error: () => {
                patchState(store, { cargandoVentas: false });
                toastSvc.error('Error', 'No se pudo cargar el historial de ventas');
              }
            }),
            catchError(() => EMPTY)
          )
        )
      )
    ),

    anularVenta(id: number, req: AnularVentaRequest): void {
      api.anularVenta(id, req).subscribe({
        next: () => {
          toastSvc.success('Venta anulada', `La venta #${id} fue anulada correctamente`);
          // Recargar la lista con los filtros actuales
          patchState(store, {
            ventas: store.ventas().map(v =>
              v.id === id ? { ...v, estado: 'ANULADA' as const, motivoAnulacion: req.motivo } : v
            )
          });
        },
        error: (e: HttpErrorResponse) =>
          toastSvc.error('Error', e.error?.mensaje ?? 'No se pudo anular la venta'),
      });
    },

    // ── Escaneo del kit ─────────────────────────────────────────────────────

    /**
     * Busca el kit por serial y valida su estado antes de aceptarlo.
     * Validación en tiempo real: si estado !== DISPONIBLE, muestra error
     * inmediatamente sin esperar al backend del registro de venta.
     */
    escanearKit: rxMethod<string>(
      pipe(
        tap(() => patchState(store, {
          estadoEscaneo: 'BUSCANDO',
          kitValidado:   null,
          errorEscaneo:  null,
        })),
        switchMap(serie =>
          api.buscarKitPorSerial(serie).pipe(
            tap({
              next: kit => {
                if (kit.estado === 'DISPONIBLE') {
                  patchState(store, {
                    estadoEscaneo: 'DISPONIBLE',
                    kitValidado: {
                      id:             kit.id,
                      serieMaestro:   kit.serieMaestro,
                      serieSim:       kit.serieSim,
                      serieDeco:      kit.serieDeco,
                      productoNombre: kit.productoNombre,
                      modeloCodigo:   kit.modeloKitCodigo,
                      estado:         kit.estado,
                      sucursalNombre: kit.sucursalActualNombre,
                    },
                    errorEscaneo: null,
                  });
                } else {
                  // Validación en tiempo real — no disponible
                  const msg = kit.estado === 'VENDIDO'
                    ? `El kit ${kit.serieMaestro} ya tiene una venta activa`
                    : `El kit ${kit.serieMaestro} no está disponible (${kit.estado})`;
                  patchState(store, {
                    estadoEscaneo: 'NO_DISPONIBLE',
                    kitValidado:   null,
                    errorEscaneo:  msg,
                  });
                }
              },
              error: (e: HttpErrorResponse) => {
                patchState(store, {
                  estadoEscaneo: 'NO_ENCONTRADO',
                  kitValidado:   null,
                  errorEscaneo:  e.status === 404
                    ? `Serial "${serie}" no encontrado en el sistema`
                    : 'Error al buscar el kit',
                });
              }
            }),
            catchError(() => EMPTY)
          )
        )
      )
    ),

    limpiarKit(): void {
      patchState(store, {
        estadoEscaneo: 'IDLE', kitValidado: null, errorEscaneo: null,
      });
    },

    // ── Autocomplete de clientes ────────────────────────────────────────────

    buscarClientes: rxMethod<string>(
      pipe(
        debounceTime(280),
        distinctUntilChanged(),
        tap(() => patchState(store, { buscandoClientes: true })),
        switchMap(q => {
          if (q.trim().length < 2) {
            patchState(store, { clientesBusqueda: [], buscandoClientes: false });
            return EMPTY;
          }
          return api.buscarClientes(q).pipe(
            tap({
              next:  clientes => patchState(store, { clientesBusqueda: clientes, buscandoClientes: false }),
              error: ()       => patchState(store, { clientesBusqueda: [], buscandoClientes: false }),
            }),
            catchError(() => EMPTY)
          );
        })
      )
    ),

    seleccionarCliente(cliente: ClienteResponse): void {
      patchState(store, {
        clienteSeleccionado: cliente,
        clientesBusqueda:    [],
      });
    },

    limpiarCliente(): void {
      patchState(store, {
        clienteSeleccionado: null,
        clientesBusqueda:    [],
      });
    },

    // ── Vendedores ──────────────────────────────────────────────────────────

    cargarVendedores: rxMethod<number>(
      pipe(
        switchMap(zonaId =>
          api.getVendedoresPorZona(zonaId).pipe(
            tap({ next: vendedores => patchState(store, { vendedores }) }),
            catchError(() => EMPTY)
          )
        )
      )
    ),

    // ── Registrar venta ─────────────────────────────────────────────────────

    registrarVenta: rxMethod<RegistrarVentaRequest>(
      pipe(
        tap(() => patchState(store, { guardando: true, ventaExito: null, error: null })),
        switchMap(req =>
          api.registrarVenta(req).pipe(
            tap({
              next: res => {
                patchState(store, {
                  guardando:  false,
                  ventaExito: {
                    ventaId:      res.ventaId,
                    serieMaestro: res.serieMaestro,
                    cliente:      res.clienteNombreCompleto,
                  },
                  estadoEscaneo:       'IDLE',
                  kitValidado:         null,
                  clienteSeleccionado: null,
                });
                toastSvc.success(
                  '¡Venta registrada!',
                  `Kit ${res.serieMaestro} → ${res.clienteNombreCompleto}`
                );
              },
              error: (e: HttpErrorResponse) => {
                patchState(store, {
                  guardando: false,
                  error: e.error?.mensaje ?? 'Error al registrar la venta',
                });
                toastSvc.error(
                  'Error al registrar',
                  e.error?.mensaje ?? 'Inténtalo de nuevo'
                );
              }
            }),
            catchError(() => EMPTY)
          )
        )
      )
    ),

    limpiarVentaExito(): void {
      patchState(store, { ventaExito: null });
    },

    limpiarError(): void {
      patchState(store, { error: null });
    },

  }))
);
