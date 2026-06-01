import { Routes } from '@angular/router';
import { loginGuard } from '../../core/guards/guards';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    canActivate: [loginGuard],
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
    title: 'Iniciar Sesión — TyG Kit Prepago',
  },
];
