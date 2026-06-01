import {
  ChangeDetectionStrategy, Component,
  OnInit, OnDestroy, inject, signal, computed
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { InventarioStore } from '../../inventario.store';
import { ZonaContextService } from '../../../../shared/services/zona-context.service';
import { EstadoBadgeComponent } from '../../../../shared/components/ui/estado-badge/estado-badge.component';
import { FechaLocalPipe } from '../../../../shared/pipes/pipes';

@Component({
  selector:        'app-detalle-lote',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [EstadoBadgeComponent, FechaLocalPipe],
  templateUrl:     './detalle-lote.component.html',
  styleUrl:        './detalle-lote.component.css',
})
export class DetalleLoteComponent implements OnInit, OnDestroy {

  private readonly route   = inject(ActivatedRoute);
  private readonly router  = inject(Router);
  readonly store   = inject(InventarioStore);
  readonly zonaCtx = inject(ZonaContextService);

  // Filtro local de la tabla de kits por estado
  readonly filtroEstado = signal<string>('');

  /** Kits filtrados por el selector de estado */
  readonly kitsFiltrados = computed(() => {
    const estado = this.filtroEstado();
    const kits   = this.store.kitsLote();
    return estado ? kits.filter(k => k.estado === estado) : kits;
  });

  /** Conteo de kits por estado para el resumen */
  readonly resumenEstados = computed(() => {
    const kits = this.store.kitsLote();
    return {
      total:       kits.length,
      disponible:  kits.filter(k => k.estado === 'DISPONIBLE').length,
      vendido:     kits.filter(k => k.estado === 'VENDIDO').length,
      transito:    kits.filter(k => k.estado === 'TRANSITO').length,
      defectuoso:  kits.filter(k => k.estado === 'DEFECTUOSO').length,
    };
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      // Si los lotes no están cargados todavía, cargarlos primero
      if (!this.store.lotes().length) {
        this.store.cargarLotes({ zonaId: null, periodo: null });
      }
      this.store.cargarDetalleLote(id);
    }
  }

  ngOnDestroy(): void {
    this.store.limpiarDetalle();
  }

  volver(): void {
    this.router.navigate(['/inventario/lotes']);
  }

  setFiltroEstado(estado: string): void {
    this.filtroEstado.set(estado === this.filtroEstado() ? '' : estado);
  }

  /** N° Operación solo visible para ADMIN y CONTADOR */
  get verNumeroOperacion(): boolean {
    return this.zonaCtx.tieneRol('ADMIN', 'CONTADOR');
  }
}
