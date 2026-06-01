import {
  HttpRequest,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpErrorResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TokenService } from '../services/token.service';

// ── JWT Interceptor ───────────────────────────────────────────────────────────
export const jwtInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
) => {
  const tokenSvc = inject(TokenService);
  const token = tokenSvc.obtenerToken();

  if (!token || req.url.includes('/auth/login')) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    }),
  );
};

// ── Error Interceptor ─────────────────────────────────────────────────────────
export const errorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
) => {
  const router = inject(Router);
  const tokenSvc = inject(TokenService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      // Token expirado en una ruta protegida
      if (err.status === 401 && !req.url.includes('/auth/login')) {
        tokenSvc.limpiar();
        router.navigate(['/login'], { queryParams: { sesionExpirada: true } });
      }
      return throwError(() => err);
    }),
  );
};
