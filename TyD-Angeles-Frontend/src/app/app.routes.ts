import { Routes } from '@angular/router';
import { authGuard, rolGuard } from './core/guards/guards';

export const routes: Routes = [

  // ── Públicas ────────────────────────────────────────────────────────────
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: '',
    loadChildren: () =>
      import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },

  // ── Protegidas: envueltas en AppShell (sidebar + topbar) ─────────────────
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/components/layout/app-shell/app-shell.component')
        .then(m => m.AppShellComponent),

    children: [

      {
        path: 'dashboard',
        canActivate: [rolGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes')
            .then(m => m.DASHBOARD_ROUTES)
      },
      {
        path: 'inventario',
        canActivate: [rolGuard],
        data: { roles: ['ADMIN','JEFE_ALMACEN','ALMACENERO','CONTADOR'] },
        loadChildren: () =>
          import('./features/inventario/inventario.routes')
            .then(m => m.INVENTARIO_ROUTES)
      },
      {
        path: 'logistica',
        canActivate: [rolGuard],
        data: { roles: ['ADMIN','JEFE_ALMACEN','ALMACENERO'] },
        loadChildren: () =>
          import('./features/logistica/logistica.routes')
            .then(m => m.LOGISTICA_ROUTES)
      },
      {
        path: 'ventas',
        canActivate: [rolGuard],
        data: { roles: ['ADMIN','JEFE_ALMACEN','ALMACENERO','VENDEDOR'] },
        loadChildren: () =>
          import('./features/ventas/ventas.routes')
            .then(m => m.VENTAS_ROUTES)
      },
      {
        path: 'reportes',
        canActivate: [rolGuard],
        data: { roles: ['ADMIN','JEFE_ALMACEN','CONTADOR'] },
        loadChildren: () =>
          import('./features/reportes/reportes.routes')
            .then(m => m.REPORTES_ROUTES)
      },

    ]
  },

  { path: '**', redirectTo: 'login' }
];
