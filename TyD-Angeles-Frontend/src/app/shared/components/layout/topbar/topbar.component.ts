import {
  ChangeDetectionStrategy, Component,
  EventEmitter, inject, Output
} from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { ZonaContextService } from '../../../services/zona-context.service';
import { AuthStore } from '../../../../features/auth/auth.store';

@Component({
  selector:        'app-topbar',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [RouterModule],
  templateUrl:     './topbar.component.html',
  styleUrl:        './topbar.component.css',
})
export class TopbarComponent {

  @Output() toggleSidebar = new EventEmitter<void>();

  readonly zonaCtx   = inject(ZonaContextService);
  readonly authStore = inject(AuthStore);
  readonly router    = inject(Router);

  /** Nombre de la ruta actual para mostrar en el breadcrumb */
  get rutaActual(): string {
    const url = this.router.url.split('/')[1] ?? '';
    const nombres: Record<string, string> = {
      dashboard:  'Dashboard',
      inventario: 'Inventario',
      logistica:  'Logística',
      ventas:     'Ventas',
      reportes:   'Reportes',
    };
    return nombres[url] ?? 'Inicio';
  }

  get usuario() {
    return this.authStore.usuario();
  }

  logout(): void {
    this.authStore.logout();
  }
}
