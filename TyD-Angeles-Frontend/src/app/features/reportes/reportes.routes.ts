import { Routes } from '@angular/router';
import { ReportesLayoutComponent } from './reportes-layout.component';

export const REPORTES_ROUTES: Routes = [
  {
    path: '',
    component: ReportesLayoutComponent,
    children: [
      {
        path: 'ventas',
        loadComponent: () =>
          import('./components/tabla-ventas/tabla-ventas.component')
            .then(m => m.TablaVentasComponent),
        title: 'Reporte de Ventas — TyD'
      },
      {
        path: 'kardex',
        loadComponent: () =>
          import('./components/kardex-mensual/kardex-mensual.component')
            .then(m => m.KardexMensualComponent),
        title: 'Kardex Mensual — TyD'
      },
      { path: '', redirectTo: 'ventas', pathMatch: 'full' },
    ]
  }
];
