import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { TokenService } from '../services/token.service';
import { Rol, RUTA_POR_ROL } from '../../models/auth.model';

// ── Auth Guard ────────────────────────────────────────────────────────────────
export const authGuard: CanActivateFn = () => {
  const tokenSvc = inject(TokenService);
  const router = inject(Router);

  if (!tokenSvc.tieneToken() || tokenSvc.estaExpirado()) {
    tokenSvc.limpiar();
    return router.createUrlTree(['/login']);
  }
  return true;
};

// ── Rol Guard ─────────────────────────────────────────────────────────────────
export const rolGuard: CanActivateFn = (route) => {
  const tokenSvc = inject(TokenService);
  const router = inject(Router);
  const rolesPermitidos: Rol[] = route.data?.['roles'] ?? [];

  const sesion = tokenSvc.obtenerSesion();
  if (!sesion) return router.createUrlTree(['/login']);

  if (!rolesPermitidos.length || rolesPermitidos.includes(sesion.rol)) {
    return true;
  }

  return router.createUrlTree([RUTA_POR_ROL[sesion.rol] ?? '/']);
};

// ── Login Guard (evita ir al login si ya está autenticado) ────────────────────
export const loginGuard: CanActivateFn = () => {
  const tokenSvc = inject(TokenService);
  const router = inject(Router);

  if (tokenSvc.tieneToken() && !tokenSvc.estaExpirado()) {
    const sesion = tokenSvc.obtenerSesion();
    return router.createUrlTree([sesion ? RUTA_POR_ROL[sesion.rol] : '/']);
  }
  return true;
};
