import {
  ChangeDetectionStrategy, Component,
  EventEmitter, inject, Output, signal
} from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ZonaContextService } from '../../../services/zona-context.service';
import { AuthStore } from '../../../../features/auth/auth.store';
import { Rol } from '../../../../models/auth.model';
import {NgOptimizedImage, NgSwitch} from '@angular/common';

interface NavItem {
  label:   string;
  ruta:    string;
  icono:   string;          // nombre del SVG path
  roles:   Rol[];
  hijos?:  NavItem[];
}

const NAV_ITEMS: NavItem[] = [
  {
    label: 'Dashboard',
    ruta:  '/dashboard',
    icono: 'home',
    roles: ['ADMIN'],
  },
  {
    label: 'Inventario',
    ruta:  '/inventario',
    icono: 'inventory',
    roles: ['ADMIN', 'JEFE_ALMACEN', 'ALMACENERO', 'CONTADOR'],
  },
  {
    label: 'Logística',
    ruta:  '/logistica',
    icono: 'logistics',
    roles: ['ADMIN', 'JEFE_ALMACEN', 'ALMACENERO'],
  },
  {
    label: 'Ventas',
    ruta:  '/ventas',
    icono: 'sales',
    roles: ['ADMIN', 'JEFE_ALMACEN', 'ALMACENERO', 'VENDEDOR'],
  },
  {
    label: 'Reportes',
    ruta:  '/reportes',
    icono: 'reports',
    roles: ['ADMIN', 'JEFE_ALMACEN', 'CONTADOR'],
  },
];

@Component({
  selector:        'app-sidebar',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [RouterLink, RouterLinkActive, NgSwitch, NgOptimizedImage],
  templateUrl:     './sidebar.component.html',
  styleUrl:        './sidebar.component.css',
})
export class SidebarComponent {

  @Output() cerrar = new EventEmitter<void>();

  readonly zonaCtx   = inject(ZonaContextService);
  readonly authStore = inject(AuthStore);
  readonly colapsar  = signal(false);

  get itemsVisibles(): NavItem[] {
    const rol = this.zonaCtx.rol();
    if (!rol) return [];
    return NAV_ITEMS.filter(item => item.roles.includes(rol));
  }

  get usuario() {
    return this.authStore.usuario();
  }

  toggleColapsar(): void {
    this.colapsar.update(v => !v);
  }

  logout(): void {
    this.authStore.logout();
  }

  onNavClick(): void {
    // Emitir en móvil para cerrar el sidebar
    this.cerrar.emit();
  }
}
