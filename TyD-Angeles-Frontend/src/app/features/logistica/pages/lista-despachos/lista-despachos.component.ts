import {
  ChangeDetectionStrategy, Component,
  OnInit, inject, signal, computed
} from '@angular/core';
import { Router } from '@angular/router';
import { LogisticaStore } from '../../logistica.store';
import { ZonaContextService } from '../../../../shared/services/zona-context.service';
import { EstadoBadgeComponent } from '../../../../shared/components/ui/estado-badge/estado-badge.component';
import { FechaLocalPipe } from '../../../../shared/pipes/pipes';

type TabActiva = 'pendientes' | 'todos';

@Component({
  selector:        'app-lista-despachos',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [EstadoBadgeComponent, FechaLocalPipe],
  templateUrl:     './lista-despachos.component.html',
  styleUrl:        './lista-despachos.component.css',
})
export class ListaDespachosComponent implements OnInit {

  readonly store   = inject(LogisticaStore);
  readonly zonaCtx = inject(ZonaContextService);
  readonly router  = inject(Router);

  readonly tabActiva = signal<TabActiva>('pendientes');

  readonly puedeCrearDespacho = computed(() =>
    this.zonaCtx.tieneRol('ADMIN', 'JEFE_ALMACEN', 'ALMACENERO')
  );

  ngOnInit(): void {
    this.store.cargarPendientes();
    this.store.cargarDespachos();
  }

  cambiarTab(tab: TabActiva): void {
    this.tabActiva.set(tab);
  }

  irACrear(): void {
    this.router.navigate(['/logistica/nuevo']);
  }

  irAConfirmar(despachoId: number): void {
    this.router.navigate(['/logistica/despachos', despachoId]);
  }

  /** Solo se puede confirmar si el despacho está EN_TRANSITO */
  puedeConfirmar(estado: string): boolean {
    return estado === 'EN_TRANSITO';
  }
}
