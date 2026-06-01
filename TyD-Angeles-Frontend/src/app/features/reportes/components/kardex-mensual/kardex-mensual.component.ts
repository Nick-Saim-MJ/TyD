import {
  ChangeDetectionStrategy, Component, OnInit,
  inject, signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportesStore }     from '../../store/reportes.store';
import { ExcelService }      from '../../services/excel.service';
import { ReporteHttpService } from '../../services/reporte-http.service';
import { KardexDetalle, KardexResumen } from '../../models/reporte.models';

@Component({
  selector: 'app-kardex-mensual',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './kardex-mensual.component.html',
  styleUrls: ['./kardex-mensual.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KardexMensualComponent implements OnInit {
  protected readonly store = inject(ReportesStore);
  private  readonly excel = inject(ExcelService);
  private  readonly http  = inject(ReporteHttpService);

  // Filtros locales
  filtros = {
    sucursalId: null as number | null,
    modeloKit:  null as 'LH100' | 'LH300' | null,
    periodo:    this._periodoActual(),
  };

  readonly periodoMin = '2023-01';
  readonly periodoMax = this._periodoActual();

  // Detalle expandible
  detalleMap = signal<Map<string, KardexDetalle[]>>(new Map());
  loadingDetalle = signal<Set<string>>(new Set());

  // Confirmación de cierre
  kardexACerrar = signal<KardexResumen | null>(null);
  cerrando      = signal(false);
  mensajeCierre = signal<string | null>(null);

  ngOnInit(): void {
    this.store.cargarSucursales(undefined);
    this.buscar();
  }

  buscar(): void {
    this.store.setFiltroKardex({
      sucursalId: this.filtros.sucursalId,
      modeloKit:  this.filtros.modeloKit,
      periodo:    this.filtros.periodo,
    });
    this.store.buscarKardex(undefined);
  }

  limpiar(): void {
    this.filtros = { sucursalId: null, modeloKit: null, periodo: this._periodoActual() };
    this.buscar();
  }

  // ── Toggle detalle ────────────────────────────────────────────────────
  toggleDetalle(k: KardexResumen): void {
    const key = this._key(k);
    const map = new Map(this.detalleMap());
    if (map.has(key)) {
      map.delete(key);
      this.detalleMap.set(map);
      return;
    }

    const loading = new Set(this.loadingDetalle());
    loading.add(key);
    this.loadingDetalle.set(loading);

    this.http.getKardexDetalle(k.sucursalId, k.modeloKit, k.periodo)
      .subscribe({
        next: detalle => {
          const m = new Map(this.detalleMap());
          m.set(key, detalle);
          this.detalleMap.set(m);

          const l = new Set(this.loadingDetalle());
          l.delete(key);
          this.loadingDetalle.set(l);
        },
        error: () => {
          const l = new Set(this.loadingDetalle());
          l.delete(key);
          this.loadingDetalle.set(l);
        }
      });
  }

  isExpanded(k: KardexResumen): boolean {
    return this.detalleMap().has(this._key(k));
  }

  isLoadingDetalle(k: KardexResumen): boolean {
    return this.loadingDetalle().has(this._key(k));
  }

  getDetalle(k: KardexResumen): KardexDetalle[] {
    return this.detalleMap().get(this._key(k)) ?? [];
  }

  // ── Cierre de período ─────────────────────────────────────────────────
  solicitarCierre(k: KardexResumen): void {
    this.kardexACerrar.set(k);
    this.mensajeCierre.set(null);
  }

  cancelarCierre(): void {
    this.kardexACerrar.set(null);
  }

  confirmarCierre(): void {
    const k = this.kardexACerrar();
    if (!k) return;

    this.cerrando.set(true);

    // ✅ Ahora usamos el ID del kardex
    this.http.cerrarKardex(k.id)
      .subscribe({
        next: () => {
          this.cerrando.set(false);
          this.kardexACerrar.set(null);
          this.mensajeCierre.set(`Kardex cerrado: ${k.sucursalNombre} — ${k.modeloKit} — ${k.periodo}`);
          this.store.buscarKardex(undefined);
          setTimeout(() => this.mensajeCierre.set(null), 5000);
        },
        error: () => {
          this.cerrando.set(false);
          this.mensajeCierre.set('Error al cerrar el kardex. Intente nuevamente.');
        }
      });
  }

  // ── Exportar ──────────────────────────────────────────────────────────
  exportar(): void {
    this.excel.exportarKardex(
      this.store.kardexResumen(),
      this.filtros.periodo ?? 'todos'
    );
  }

  // ── Helpers ───────────────────────────────────────────────────────────
  tipoMovimientoClass(tipo: string): string {
    return { ENTRADA: 'mov--entrada', SALIDA: 'mov--salida', AJUSTE: 'mov--ajuste' }[tipo] ?? '';
  }

  variacionKardex(k: KardexResumen): number {
    return k.stockFin - k.stockInicio;
  }

  private _key(k: KardexResumen): string {
    return `${k.sucursalId}-${k.modeloKit}-${k.periodo}`;
  }

  private _periodoActual(): string {
    return new Date().toISOString().slice(0, 7);
  }
}
