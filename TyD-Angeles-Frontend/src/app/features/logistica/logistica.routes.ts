import { Routes } from '@angular/router';

export const LOGISTICA_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'pendientes',
    pathMatch: 'full',
  },
  {
    path: 'pendientes',
    title: 'Recepciones Pendientes',
    loadComponent: () =>
      import('./pages/lista-despachos/lista-despachos.component')
        .then(m => m.ListaDespachosComponent),
  },
  {
    path: 'nuevo',
    title: 'Crear Despacho',
    loadComponent: () =>
      import('./pages/crear-despacho/crear-despacho.component')
        .then(m => m.CrearDespachoComponent),
  },
  {
    path: 'despachos/:id',
    title: 'Confirmar Recepción',
    loadComponent: () =>
      import('./pages/confirmar-recepcion/confirmar-recepcion.component')
        .then(m => m.ConfirmarRecepcionComponent),
  },
  {
    path: 'mis-recepciones',
    title: 'Mis Recepciones',
    loadComponent: () =>
      import('./pages/mis-recepciones/mis-recepciones.component')
        .then(m => m.MisRecepcionesComponent),
  },
];
