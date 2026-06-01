import {
  ChangeDetectionStrategy, Component, OnInit,
  inject, effect
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ReportesStore } from '../../store/reportes.store';
import { ExcelService  } from '../../services/excel.service';

@Component({
  selector: 'app-tabla-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tabla-ventas.component.html',
  styleUrls: ['./tabla-ventas.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TablaVentasComponent implements OnInit {
  protected readonly store = inject(ReportesStore);
  private  readonly excel = inject(ExcelService);

  // Controles locales del formulario de filtros
  filtros = {
    zonaId:     null as number | null,
    sucursalId: null as number | null,
    vendedorId: null as number | null,
    mes:        null as string | null,
  };


  // Periodo mínimo/máximo para el input[type=month]
  readonly periodoMin = '2023-01';
  readonly periodoMax = new Date().toISOString().slice(0, 7);

  constructor() {
    // Cuando se cargan los datos para exportar, dispara la descarga
    effect(() => {
      const ventas = this.store.ventasParaExport();
      if (ventas.length > 0) {
        this.excel.exportarVentas(ventas, this._labelFiltros());
        this.store.limpiarExport();
      }
    });
  }


  ngOnInit(): void {
    this.store.cargarCatalogos(undefined);
    this.store.cargarSucursales(undefined);
    this.store.cargarVendedores(undefined);
    this.store.buscarVentas(undefined);
  }

  ngAfterViewInit(): void {
    // Detectar si la tabla tiene scroll horizontal
    const scrollContainer = document.querySelector('.tabla-scroll-container');
    if (scrollContainer) {
      const checkScroll = () => {
        if (scrollContainer.scrollWidth > scrollContainer.clientWidth) {
          scrollContainer.classList.add('has-scroll');
        } else {
          scrollContainer.classList.remove('has-scroll');
        }
      };

      checkScroll();
      window.addEventListener('resize', checkScroll);
      scrollContainer.addEventListener('scroll', checkScroll);
    }
  }

  onZonaChange(): void {
    this.filtros.sucursalId = null;
    this.filtros.vendedorId = null;
    this.store.setFiltroVentas({ zonaId: this.filtros.zonaId });
    this.store.cargarSucursales(this.filtros.zonaId ?? undefined);
    this.store.cargarVendedores(undefined);
  }

  onSucursalChange(): void {
    this.filtros.vendedorId = null;
    this.store.setFiltroVentas({ sucursalId: this.filtros.sucursalId });
    this.store.cargarVendedores(this.filtros.sucursalId ?? undefined);
  }

  onVendedorChange(): void {
    this.store.setFiltroVentas({ vendedorId: this.filtros.vendedorId });
  }

  onMesChange(): void {
    this.store.setFiltroVentas({ mes: this.filtros.mes });
  }

  buscar(): void {
    this.store.buscarVentas(undefined);
  }

  limpiarFiltros(): void {
    this.filtros = { zonaId: null, sucursalId: null, vendedorId: null, mes: null };
    this.store.setFiltroVentas({ zonaId: null, sucursalId: null, vendedorId: null, mes: null });
    this.store.buscarVentas(undefined);
  }

  exportar(): void {
    // Carga todos los registros (sin paginación) y el effect dispara la descarga
    this.store.cargarVentasExport(undefined);
  }

  irPagina(pagina: number): void {
    this.store.setPage(pagina);
    this.store.buscarVentas(undefined);
  }

  get paginas(): number[] {
    const total = this.store.ventasPage()?.totalPages ?? 0;
    return Array.from({ length: total }, (_, i) => i);
  }

  estadoClass(estado: string): string {
    return {
      'ACTIVA':    'badge--verde',
      'ANULADA':   'badge--rojo',
      'PENDIENTE': 'badge--amarillo',
      'ACTIVADO':  'badge--verde',
      'RECHAZADO': 'badge--rojo',
    }[estado] ?? '';
  }

  private _labelFiltros(): string {
    const partes: string[] = [];
    const z = this.store.zonas().find(z => z.id === this.filtros.zonaId);
    const s = this.store.sucursales().find(s => s.id === this.filtros.sucursalId);
    const v = this.store.vendedores().find(v => v.id === this.filtros.vendedorId);
    if (z) partes.push(`Zona: ${z.nombre}`);
    if (s) partes.push(`Oficina: ${s.nombre}`);
    if (v) partes.push(`Vendedor: ${v.nombres}`);
    if (this.filtros.mes) partes.push(`Mes: ${this.filtros.mes}`);
    return partes.length ? partes.join(' | ') : 'Sin filtros';
  }

}
