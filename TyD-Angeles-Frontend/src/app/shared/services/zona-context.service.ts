// shared/services/zona-context.service.ts
import { computed, inject, Injectable } from '@angular/core';
import { AuthStore } from '../../features/auth/auth.store';
import { Rol } from '../../models/auth.model';

@Injectable({ providedIn: 'root' })
export class ZonaContextService {
  private readonly store = inject(AuthStore);

  // ── Zona filtrada por rol (para UI) ──────────────────────────────────
  readonly zonaId = computed(() => {
    const u = this.store.usuario();
    if (!u) return null;
    return this.sinFiltroZona(u.rol) ? null : u.zonaId;
  });

  // ── Zona REAL del usuario (siempre, para lógica de negocio) ─────────
  readonly zonaIdReal = computed(() => this.store.usuario()?.zonaId ?? null);
  readonly zonaNombre = computed(() => this.store.usuario()?.zonaNombre ?? null);

  // ── Sucursal del usuario ───────────────────────────────────────────
  readonly sucursalId = computed(() => this.store.usuario()?.sucursalId ?? null);
  readonly sucursalNombre = computed(() => this.store.usuario()?.sucursalNombre ?? null);

  // ➕ AÑADIR ESTA LÍNEA:
  readonly usuarioId = computed(() => this.store.usuario()?.id ?? null);

  // ── Rol y permisos ─────────────────────────────────────────────────
  readonly rol = computed(() => this.store.usuario()?.rol ?? null);
  readonly permisos = computed(() => this.store.usuario()?.permisos ?? []);

  // ── Helpers de acceso ──────────────────────────────────────────────
  readonly puedeVerTodasLasZonas = computed(() => {
    const rol = this.store.usuario()?.rol;
    return rol ? this.sinFiltroZona(rol) : false;
  });

  /** ¿Puede ver datos de esta zona? */
  puedeVerZona(zonaId: number | null): boolean {
    if (this.puedeVerTodasLasZonas()) return true;
    return zonaId === this.zonaIdReal();
  }

  /** ¿Puede ver datos de esta sucursal? */
  puedeVerSucursal(sucursalId: number | null): boolean {
    if (this.puedeVerTodasLasZonas()) return true;
    // ALMACENERO solo ve su sucursal; JEFE_ALMACEN ve todas las de su zona
    if (this.tieneRol('ALMACENERO')) {
      return sucursalId === this.sucursalId();
    }
    // Para JEFE_ALMACEN, la validación de zona se hace en backend
    return true;
  }

  /** Verifica si el usuario tiene uno de los roles indicados */
  tieneRol(...roles: Rol[]): boolean {
    const rol = this.store.usuario()?.rol;
    return rol ? roles.includes(rol) : false;
  }

  /** Verifica si el usuario tiene un permiso específico */
  tienePermiso(permiso: string): boolean {
    return this.permisos().includes(permiso);
  }

  /** ¿Este rol debe tener filtro de zona? */
  private sinFiltroZona(rol: Rol): boolean {
    return rol === 'ADMIN' || rol === 'CONTADOR';
  }

}
