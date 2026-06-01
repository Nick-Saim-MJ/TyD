import {
  ChangeDetectionStrategy, Component, signal
} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent }       from '../sidebar/sidebar.component';
import { TopbarComponent }        from '../topbar/topbar.component';
import { ToastContainerComponent } from '../../ui/toast-container/toast-container.component';
import { ConfirmModalComponent }   from '../../ui/confirm-modal/confirm-modal.component';

/**
 * Shell principal de la aplicación.
 * Envuelve todas las rutas protegidas con:
 *   - Sidebar de navegación (colapsable)
 *   - Topbar con info del usuario y zona
 *   - <router-outlet> para el contenido
 *   - ToastContainer para notificaciones globales
 *   - ConfirmModal para diálogos de confirmación
 *
 * Uso en app.routes.ts:
 *   {
 *     path: '',
 *     component: AppShellComponent,
 *     canActivate: [authGuard],
 *     children: [
 *       { path: 'inventario', ... },
 *       { path: 'ventas', ... },
 *     ]
 *   }
 */
@Component({
  selector:        'app-shell',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterOutlet,
    SidebarComponent,
    TopbarComponent,
    ToastContainerComponent,
    ConfirmModalComponent
  ],
  templateUrl: './app-shell.component.html',
  styleUrl:    './app-shell.component.css',
})
export class AppShellComponent {
  readonly sidebarAbierto = signal(false);

  toggleSidebar(): void {
    this.sidebarAbierto.update(v => !v);
  }

  cerrarSidebar(): void {
    this.sidebarAbierto.set(false);
  }
}
