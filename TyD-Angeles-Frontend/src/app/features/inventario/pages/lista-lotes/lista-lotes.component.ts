import {
  ChangeDetectionStrategy, Component,
  OnInit, inject, signal, computed
} from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InventarioStore } from '../../inventario.store';
import { ZonaContextService } from '../../../../shared/services/zona-context.service';
import { FechaLocalPipe } from '../../../../shared/pipes/pipes';

@Component({
  selector:        'app-lista-lotes',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [FormsModule, FechaLocalPipe],
  templateUrl:     './lista-lotes.component.html',
  styleUrl:        './lista-lotes.component.css',
})
export class ListaLotesComponent implements OnInit {

  readonly store   = inject(InventarioStore);
  readonly zonaCtx = inject(ZonaContextService);
  readonly router  = inject(Router);

  // ── Filtros locales de la vista ──────────────────────────────────────────
  filtroPeriodo = signal<string>('');
  readonly filtroZonaId  = signal<number | null>(null);

  /** ADMIN y CONTADOR ven el selector de zona; el resto ve solo su zona */
  readonly puedeSeleccionarZona = computed(() =>
    this.zonaCtx.tieneRol('ADMIN', 'CONTADOR')
  );

  /** Texto del subtítulo de zona */
  readonly labelZona = computed(() =>
    this.zonaCtx.puedeVerTodasLasZonas()
      ? 'Todas las zonas'
      : (this.zonaCtx.zonaNombre() ?? '')
  );

  /** Zonas disponibles (hardcoded para el selector — en producción cargar desde API) */
  readonly zonasDisponibles = [
    { id: 1, nombre: 'Puno'          },
    { id: 2, nombre: 'Cusco'         },
    { id: 3, nombre: 'Madre de Dios' },
    { id: 4, nombre: 'Apurímac'      },
  ];

  // ── Computed: porcentaje de recepción por lote ───────────────────────────
  porcentaje(esperada: number, recibida: number): number {
    if (!esperada) return 0;
    return Math.round((recibida / esperada) * 100);
  }

  // ── Ciclo de vida ─────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.buscar();
  }

  // ── Acciones ──────────────────────────────────────────────────────────────
  buscar(): void {
    this.store.cargarLotes({
      zonaId:  this.filtroZonaId(),
      periodo: this.filtroPeriodo() || null,
    });
  }

  limpiar(): void {
    this.filtroPeriodo.set('');
    this.filtroZonaId.set(null);
    this.buscar();
  }

  verDetalle(loteId: number): void {
    this.router.navigate(['/inventario/lotes', loteId]);
  }

  irBoucher(): void {
    this.router.navigate(['/inventario/boucher']);
  }

  exportar(): void {
    this.store.exportarExcel(this.filtroZonaId(), this.filtroPeriodo() || null);
  }

  onZonaChange(event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    this.filtroZonaId.set(val ? +val : null);
  }
}
