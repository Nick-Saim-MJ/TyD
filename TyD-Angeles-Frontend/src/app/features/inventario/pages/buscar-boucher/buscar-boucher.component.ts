import {
  ChangeDetectionStrategy, Component,
  OnDestroy, inject, signal
} from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { InventarioStore } from '../../inventario.store';
import { ZonaContextService } from '../../../../shared/services/zona-context.service';
import { EstadoBadgeComponent } from '../../../../shared/components/ui/estado-badge/estado-badge.component';
import { FechaLocalPipe } from '../../../../shared/pipes/pipes';

@Component({
  selector:        'app-buscar-boucher',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [FormsModule, EstadoBadgeComponent, FechaLocalPipe],
  templateUrl:     './buscar-boucher.component.html',
  styleUrl:        './buscar-boucher.component.css',
})
export class BuscarBoucherComponent implements OnDestroy {

  readonly store   = inject(InventarioStore);
  readonly zonaCtx = inject(ZonaContextService);
  readonly router  = inject(Router);

  /** Valor del input de búsqueda */
  termino = signal<string>('');

  ngOnDestroy(): void {
    this.store.limpiarBoucher();
  }

  buscar(): void {
    const t = this.termino().trim();
    if (!t) return;
    this.store.buscarPorBoucher(t);
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') this.buscar();
  }

  limpiar(): void {
    this.termino.set('');
    this.store.limpiarBoucher();
  }

  volver(): void {
    this.router.navigate(['/inventario/lotes']);
  }

  /** Cuenta los kits por estado en un grupo */
  pct(parte: number, total: number): number {
    return total ? Math.round((parte / total) * 100) : 0;
  }
}
