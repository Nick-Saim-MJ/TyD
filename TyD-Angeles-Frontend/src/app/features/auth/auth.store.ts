import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, EMPTY } from 'rxjs';
import { AuthApiService } from '../../api/auth.api';
import { TokenService } from '../../core/services/token.service';
import {
  LoginRequest,
  LoginResponse,
  MeResponse,
  RUTA_POR_ROL,
  UsuarioSesion,
} from '../../models/auth.model';

// ── Estado ────────────────────────────────────────────────────────────────────
interface AuthState {
  usuario: UsuarioSesion | null;
  cargando: boolean;
  error: string | null;
  /** ISO string — cuándo se desbloquea la cuenta. null = no bloqueada */
  bloqueadoHasta: string | null;
}

const initialState: AuthState = {
  usuario: null,
  cargando: false,
  error: null,
  bloqueadoHasta: null,
};

// ── Store ─────────────────────────────────────────────────────────────────────
export const AuthStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withMethods(
    (
      store,
      authApi = inject(AuthApiService),
      tokenSvc = inject(TokenService),
      router = inject(Router),
    ) => ({
      /**
       * Login: valida credenciales → guarda token → redirige según rol.
       * Maneja 401 (credenciales inválidas) y 423 (cuenta bloqueada).
       */
      login: rxMethod<LoginRequest>(
        pipe(
          tap(() => patchState(store, { cargando: true, error: null, bloqueadoHasta: null })),

          switchMap((request) =>
            authApi.login(request).pipe(
              tap({
                next: (res: LoginResponse) => {
                  tokenSvc.guardarToken(res.token);
                  tokenSvc.guardarSesion(res);

                  const usuario: UsuarioSesion = {
                    id: res.usuarioId,
                    username: res.username,
                    nombreCompleto: res.nombreCompleto,
                    rol: res.rol,
                    zonaId: res.zonaId,
                    zonaNombre: res.zonaNombre,
                    sucursalId: res.sucursalId,
                    sucursalNombre: res.sucursalNombre,
                    permisos: [],
                  };

                  patchState(store, {
                    usuario,
                    cargando: false,
                    error: null,
                    bloqueadoHasta: null,
                  });

                  // Cargar permisos en segundo plano
                  authApi.me().subscribe((me: MeResponse) => {
                    tokenSvc.actualizarPermisos(me.permisos);
                    patchState(store, {
                      usuario: { ...store.usuario()!, permisos: me.permisos },
                    });
                  });

                  router.navigate([RUTA_POR_ROL[res.rol] ?? '/']);
                },
              }),
              catchError((err: HttpErrorResponse) => {
                if (err.status === 423) {
                  const detalles = err.error?.detalles as { bloqueadoHasta?: string } | undefined;
                  patchState(store, {
                    cargando: false,
                    error: err.error?.mensaje ?? 'Cuenta bloqueada.',
                    bloqueadoHasta: detalles?.bloqueadoHasta ?? null,
                  });
                } else {
                  patchState(store, {
                    cargando: false,
                    error:
                      err.status === 401
                        ? 'Usuario o contraseña incorrectos.'
                        : 'Error de conexión. Intenta de nuevo.',
                    bloqueadoHasta: null,
                  });
                }
                return EMPTY;
              }),
            ),
          ),
        ),
      ),

      logout(): void {
        authApi.logout().subscribe({
          complete: () => {
            tokenSvc.limpiar();
            patchState(store, initialState);
            router.navigate(['/login']);
          },
          error: () => {
            tokenSvc.limpiar();
            patchState(store, initialState);
            router.navigate(['/login']);
          },
        });
      },

      inicializarDesdeStorage(): void {
        const sesion = tokenSvc.obtenerSesion();
        if (sesion && !tokenSvc.estaExpirado()) {
          patchState(store, { usuario: sesion });
        } else {
          tokenSvc.limpiar();
        }
      },

      limpiarError(): void {
        patchState(store, { error: null, bloqueadoHasta: null });
      },
    }),
  ),
);
