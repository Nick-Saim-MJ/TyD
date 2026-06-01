import { Routes } from '@angular/router';

export const INVENTARIO_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'lotes',
    pathMatch: 'full',
  },
  {
    path: 'lotes',
    title: 'Lotes — Inventario',
    loadComponent: () =>
      import('./pages/lista-lotes/lista-lotes.component')
        .then(m => m.ListaLotesComponent),
  },
  {
    path: 'lotes/:id',
    title: 'Detalle de Lote',
    loadComponent: () =>
      import('./pages/detalle-lote/detalle-lote.component')
        .then(m => m.DetalleLoteComponent),
  },
  {
    path: 'boucher',
    title: 'Buscar por Boucher',
    loadComponent: () =>
      import('./pages/buscar-boucher/buscar-boucher.component')
        .then(m => m.BuscarBoucherComponent),
  },
];
