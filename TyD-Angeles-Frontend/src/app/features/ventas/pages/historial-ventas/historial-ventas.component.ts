import {
  ChangeDetectionStrategy, Component,
  OnInit, computed, inject, signal
} from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VentasStore } from '../../ventas.store';
import { ZonaContextService } from '../../../../shared/services/zona-context.service';
import { ModalService } from '../../../../shared/services/modal.service';
import { EstadoBadgeComponent } from '../../../../shared/components/ui/estado-badge/estado-badge.component';
import { FechaLocalPipe } from '../../../../shared/pipes/pipes';
import { TipoCliente, VentaResponse } from '../../models/ventas.model';
import {SlicePipe} from '@angular/common';

@Component({
  selector:        'app-historial-ventas',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [FormsModule, EstadoBadgeComponent, FechaLocalPipe, SlicePipe],
  templateUrl:     './historial-ventas.component.html',
  styleUrl:        './historial-ventas.component.css',
})
export class HistorialVentasComponent implements OnInit {

  readonly store    = inject(VentasStore);
  readonly zonaCtx  = inject(ZonaContextService);
  readonly modal    = inject(ModalService);
  readonly router   = inject(Router);

  // ── Filtros locales ───────────────────────────────────────────────────────
  readonly filtroMes        = signal<string>('');
  readonly filtroTipo       = signal<TipoCliente | ''>('');
  readonly filtroVendedorId = signal<number | null>(null);

  // ── Permisos ──────────────────────────────────────────────────────────────
  readonly puedeAnular = computed(() =>
    this.zonaCtx.tieneRol('ADMIN', 'JEFE_ALMACEN')
  );
  readonly puedeRegistrar = computed(() =>
    this.zonaCtx.tieneRol('ADMIN', 'JEFE_ALMACEN', 'ALMACENERO', 'VENDEDOR')
  );
  readonly puedeVerTodasLasZonas = computed(() =>
    this.zonaCtx.puedeVerTodasLasZonas()
  );

  // ── Resumen de la lista actual ────────────────────────────────────────────
  readonly resumen = computed(() => {
    const ventas = this.store.ventas();
    return {
      total:    ventas.length,
      activas:  ventas.filter(v => v.estado === 'ACTIVA').length,
      anuladas: ventas.filter(v => v.estado === 'ANULADA').length,
      monto:    ventas
        .filter(v => v.estado === 'ACTIVA')
        .reduce((s, v) => s + v.montoVenta, 0),
    };
  });

  ngOnInit(): void {
    this.buscar();
    // Cargar vendedores para el filtro
    const zonaId = this.zonaCtx.zonaId();
    if (zonaId) this.store.cargarVendedores(zonaId);
  }

  buscar(): void {
    this.store.cargarVentas({
      mes:       this.filtroMes()       || null,
      tipo:      (this.filtroTipo()     || null) as TipoCliente | null,
      vendedorId: this.filtroVendedorId() || null,
    });
  }

  limpiar(): void {
    this.filtroMes.set('');
    this.filtroTipo.set('');
    this.filtroVendedorId.set(null);
    this.buscar();
  }

  irARegistrar(): void {
    this.router.navigate(['/ventas/nueva']);
  }

  async anular(venta: VentaResponse): Promise<void> {
    const ok = await this.modal.confirmar({
      titulo:   `Anular venta #${venta.id}`,
      mensaje:  `¿Seguro que deseas anular la venta del kit ${venta.serieMaestro} a ${venta.clienteNombreCompleto}? El kit volverá a estar disponible.`,
      tipo:     'danger',
      labelConfirmar: 'Sí, anular',
      labelCancelar:  'Cancelar',
    });

    if (ok) {
      this.store.anularVenta(venta.id, { motivo: 'Anulado por administración' });
    }
  }

  onVendedorFiltroChange(event: Event): void {
    const v = (event.target as HTMLSelectElement).value;
    this.filtroVendedorId.set(v ? +v : null);
  }

  formatMonto(n: number): string {
    return `S/ ${n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
  }
}
