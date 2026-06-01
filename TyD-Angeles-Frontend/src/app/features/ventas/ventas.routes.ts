import { Routes } from '@angular/router';

export const VENTAS_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'historial',
    pathMatch: 'full',
  },
  {
    path: 'historial',
    title: 'Historial de Ventas',
    loadComponent: () =>
      import('./pages/historial-ventas/historial-ventas.component')
        .then(m => m.HistorialVentasComponent),
  },
  {
    path: 'nueva',
    title: 'Registrar Venta',
    loadComponent: () =>
      import('./pages/registrar-venta/registrar-venta.component')
        .then(m => m.RegistrarVentaComponent),
  },
];
