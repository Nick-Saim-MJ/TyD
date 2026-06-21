import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, EMPTY } from 'rxjs';
import { LogisticaApiService } from './api/logistica.api';
import { ToastService } from '../../shared/services/toast.service';
import {
  ConfirmarRecepcionRequest, CrearDespachoRequest,
  DespachoDetalleResponse, DespachoResponse,
  HistorialCustodioResponse,
  KitEnCola, SucursalOpcion
} from './models/logistica.model';
import { ItemKitResponse } from '../inventario/models/inventario.model';

// ── Estado ────────────────────────────────────────────────────────────────────
interface LogisticaState {
  despachos:         DespachoResponse[];
  pendientes:        DespachoResponse[];
  cargandoLista:     boolean;
  despachoActual:    DespachoDetalleResponse | null;
  cargandoDetalle:   boolean;
  kitsEnCola:        KitEnCola[];
  escaneando:        boolean;
  errorEscaneo:      string | null;
  sucursales:        SucursalOpcion[];
  guardando:         boolean;
  error:             string | null;

  misRecepciones:        HistorialCustodioResponse[];
  cargandoMisRecepciones: boolean;

  // ✅ Kits disponibles por ZONA (no por sucursal)
  kitsDisponibles:   ItemKitResponse[];
  cargandoKits:      boolean;
  zonaKitsCargada:   number | null;  // ← Renombrado para claridad
}

const initialState: LogisticaState = {
  despachos: [], pendientes: [], cargandoLista: false,
  despachoActual: null, cargandoDetalle: false,
  kitsEnCola: [], escaneando: false, errorEscaneo: null,
  sucursales: [], guardando: false, error: null,
  kitsDisponibles: [], cargandoKits: false, zonaKitsCargada: null,
  misRecepciones: [], cargandoMisRecepciones: false,
};

// ── Store ─────────────────────────────────────────────────────────────────────
export const LogisticaStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withMethods((store,
               api = inject(LogisticaApiService),
               toastSvc = inject(ToastService),
               router = inject(Router),
  ) => ({

    // ── Listados ────────────────────────────────────────────────────────────
    cargarDespachos: rxMethod<void>(pipe(
      tap(() => patchState(store, { cargandoLista: true, error: null })),
      switchMap(() => api.getDespachos().pipe(
        tap({
          next: despachos => patchState(store, { despachos, cargandoLista: false }),
          error: (e: HttpErrorResponse) => {
            patchState(store, { cargandoLista: false, error: e.error?.mensaje });
            toastSvc.error('Error', 'No se pudo cargar los despachos');
          }
        }),
        catchError(() => EMPTY)
      ))
    )),

    cargarPendientes: rxMethod<void>(pipe(
      tap(() => patchState(store, { cargandoLista: true })),
      switchMap(() => api.getPendientesRecepcion().pipe(
        tap({
          next: pendientes => patchState(store, { pendientes, cargandoLista: false }),
          error: () => patchState(store, { cargandoLista: false })
        }),
        catchError(() => EMPTY)
      ))
    )),

    cargarDetalle: rxMethod<number>(pipe(
      tap(() => patchState(store, { cargandoDetalle: true, despachoActual: null })),
      switchMap(id => api.getDespachoDetalle(id).pipe(
        tap({
          next: d => patchState(store, { despachoActual: d, cargandoDetalle: false }),
          error: () => {
            patchState(store, { cargandoDetalle: false });
            toastSvc.error('Error', 'No se pudo cargar el despacho');
          }
        }),
        catchError(() => EMPTY)
      ))
    )),

    cargarMisRecepciones: rxMethod<void>(pipe(
      tap(() => patchState(store, { cargandoMisRecepciones: true })),
      switchMap(() => api.getMisRecepciones().pipe(
        tap({
          next: misRecepciones => patchState(store, { misRecepciones, cargandoMisRecepciones: false }),
          error: (e: HttpErrorResponse) => {
            patchState(store, { cargandoMisRecepciones: false });
            toastSvc.error('Error', 'No se pudo cargar tu historial de recepciones');
          }
        }),
        catchError(() => EMPTY)
      ))
    )),

    cargarSucursales: rxMethod<void>(pipe(
      switchMap(() => api.getSucursales().pipe(
        tap({ next: sucursales => patchState(store, { sucursales }) }),
        catchError(() => EMPTY)
      ))
    )),

    // ── Agregar kit a la cola ───────────────────────────────────────────────
    agregarKitALaCola: (kit: ItemKitResponse) => {
      const nuevo: KitEnCola = {
        itemKitId: kit.id,
        serieMaestro: kit.serieMaestro,
        serieSim: kit.serieSim,
        serieDeco: kit.serieDeco,
        productoNombre: kit.productoNombre,
        modeloCodigo: kit.modeloKitCodigo,
      };
      patchState(store, (state) => ({
        kitsEnCola: [...state.kitsEnCola, nuevo]
      }));
    },

    // ── Escaneo de kits ─────────────────────────────────────────────────────
    escanearSerial: rxMethod<string>(pipe(
      tap(() => patchState(store, { escaneando: true, errorEscaneo: null })),
      switchMap(serie => api.buscarKitPorSerial(serie).pipe(
        tap({
          next: kit => {
            if (kit.estado !== 'DISPONIBLE') {
              patchState(store, {
                escaneando: false,
                errorEscaneo: `Kit ${kit.serieMaestro} no está disponible (${kit.estado})`,
              });
              return;
            }
            const yaAgregado = store.kitsEnCola().some(k => k.itemKitId === kit.id);
            if (yaAgregado) {
              patchState(store, {
                escaneando: false,
                errorEscaneo: `El kit ${kit.serieMaestro} ya está en la lista`,
              });
              return;
            }
            const nuevo: KitEnCola = {
              itemKitId: kit.id,
              serieMaestro: kit.serieMaestro,
              serieSim: kit.serieSim,
              serieDeco: kit.serieDeco,
              productoNombre: kit.productoNombre,
              modeloCodigo: kit.modeloKitCodigo,
            };
            patchState(store, {
              kitsEnCola: [...store.kitsEnCola(), nuevo],
              escaneando: false,
              errorEscaneo: null,
            });
            toastSvc.success('Kit agregado', kit.serieMaestro);
          },
          error: (e: HttpErrorResponse) => {
            patchState(store, {
              escaneando: false,
              errorEscaneo: e.status === 404 ? `Serial "${serie}" no encontrado` : 'Error al buscar el kit',
            });
          }
        }),
        catchError(() => EMPTY)
      ))
    )),

    // ✅ Cargar kits disponibles POR ZONA (endpoint simplificado)
    cargarKitsDisponibles: rxMethod<number>(  // ← recibe zonaId
      pipe(
        tap(zonaId => {
          console.log('📥 Cargando kits para zona:', zonaId);
          if (store.zonaKitsCargada() === zonaId) return;
          patchState(store, { cargandoKits: true });
        }),
        switchMap(zonaId =>
          api.getKitsDisponiblesSucursal(zonaId).pipe(  // ← endpoint por zona
            tap({
              next: kits => {
                console.log('✅ Kits recibidos:', kits.length);
                patchState(store, {
                  kitsDisponibles: kits,
                  cargandoKits: false,
                  zonaKitsCargada: zonaId,
                });
              },
              error: () => {
                patchState(store, { cargandoKits: false });
                toastSvc.error('No se pudieron cargar los kits disponibles');
              }
            }),
            catchError(() => EMPTY)
          )
        )
      )
    ),

    // ✅ Validar kit disponible (ahora por zonaId)
    kitDisponibleParaDespacho(kitId: number, zonaId: number): boolean {
      const kits = store.kitsDisponibles();
      const kit = kits.find(k => k.id === kitId);
      return !!kit && kit.estado === 'DISPONIBLE' && kit.zonaId === zonaId;
    },

    // ✅ Buscar kit por serial en lista cargada
    buscarKitPorSerialEnLista(serial: string): ItemKitResponse | null {
      const termino = serial.toLowerCase().trim();
      return store.kitsDisponibles().find(k =>
        k.serieMaestro?.toLowerCase() === termino ||
        k.serieSim?.toLowerCase() === termino ||
        k.serieDeco?.toLowerCase() === termino
      ) ?? null;
    },

    quitarKitDeCola(itemKitId: number): void {
      patchState(store, {
        kitsEnCola: store.kitsEnCola().filter(k => k.itemKitId !== itemKitId),
      });
    },

    limpiarCola(): void {
      patchState(store, { kitsEnCola: [], errorEscaneo: null });
    },

    limpiarErrorEscaneo(): void {
      patchState(store, { errorEscaneo: null });
    },

    // ── Crear despacho ──────────────────────────────────────────────────────
    crearDespacho: rxMethod<CrearDespachoRequest>(pipe(
      tap(() => patchState(store, { guardando: true, error: null })),
      switchMap(req => api.crearDespacho(req).pipe(
        tap({
          next: despacho => {
            patchState(store, { guardando: false, kitsEnCola: [] });
            toastSvc.success('Despacho creado', `${req.itemKitIds.length} kit(s) marcados en tránsito`);
            router.navigate(['/logistica/despachos', despacho.id]);
          },
          error: (e: HttpErrorResponse) => {
            patchState(store, { guardando: false });
            toastSvc.error('Error al crear despacho', e.error?.mensaje ?? 'Inténtalo de nuevo');
          }
        }),
        catchError(() => EMPTY)
      ))
    )),

    confirmarRecepcion(despachoId: number, req: ConfirmarRecepcionRequest): void {
      patchState(store, { guardando: true });
      api.confirmarRecepcion(despachoId, req).subscribe({
        next: (res) => {
          patchState(store, { guardando: false });
          toastSvc.success('Recepción registrada', res.mensaje);
          router.navigate(['/logistica/pendientes']);
        },
        error: (e: HttpErrorResponse) => {
          patchState(store, { guardando: false });
          toastSvc.error('Error al confirmar', e.error?.mensaje ?? 'Inténtalo de nuevo');
        }
      });
    },

    limpiarDetalle(): void {
      patchState(store, { despachoActual: null });
    },
  }))
);
