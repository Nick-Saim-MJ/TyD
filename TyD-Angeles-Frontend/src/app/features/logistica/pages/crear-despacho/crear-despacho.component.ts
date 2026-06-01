import {
  ChangeDetectionStrategy, Component, DestroyRef,
  ElementRef, OnInit, ViewChild, computed, effect,
  inject, signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LogisticaStore } from '../../logistica.store';
import { ZonaContextService } from '../../../../shared/services/zona-context.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { SucursalOpcion } from '../../models/logistica.model';
import { ItemKitResponse } from '../../../inventario/models/inventario.model';

@Component({
  selector: 'app-crear-despacho',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  templateUrl: './crear-despacho.component.html',
  styleUrl: './crear-despacho.component.css',
})
export class CrearDespachoComponent implements OnInit {
  @ViewChild('scannerInput') scannerInput!: ElementRef<HTMLInputElement>;

  readonly store = inject(LogisticaStore);
  readonly zonaCtx = inject(ZonaContextService);
  readonly router = inject(Router);
  readonly toastSvc = inject(ToastService);
  readonly destroyRef = inject(DestroyRef);

  // ── Formulario ────────────────────────────────────────────────────────────
  readonly sucursalDestinoId = signal<number | null>(null);
  readonly guiaRemision = signal<string>('');
  readonly observaciones = signal<string>('');

  // ── Scanner ───────────────────────────────────────────────────────────────
  readonly valorScanner = signal<string>('');
  private scanBuffer = '';
  private lastKeyTime = 0;
  private readonly SCANNER_THRESHOLD_MS = 50;

  // ── Selector de kits disponibles ─────────────────────────────────────────
  readonly busquedaKit = signal<string>('');
  readonly mostrarSelector = signal<boolean>(false);

  // ── Sucursales filtradas ─────────────────────────────────────────────────
  readonly sucursalesDestino = computed<SucursalOpcion[]>(() => {
    const todas = this.store.sucursales();
    const zonaId = this.zonaCtx.zonaId();
    const esAdmin = this.zonaCtx.tieneRol('ADMIN', 'JEFE_ALMACEN');
    return esAdmin ? todas : todas.filter(s => s.zonaId === zonaId);
  });

  readonly sucursalDestinoSeleccionada = computed<SucursalOpcion | null>(() => {
    const id = this.sucursalDestinoId();
    return id ? (this.store.sucursales().find(s => s.id === id) ?? null) : null;
  });

  readonly esInterZonaAlertada = computed<boolean>(() => {
    const dest = this.sucursalDestinoSeleccionada();
    const zonaU = this.zonaCtx.zonaId();
    return !!(dest && zonaU && dest.zonaId !== zonaU);
  });

  readonly puedeEnviar = computed<boolean>(() =>
    !!this.sucursalDestinoId() &&
    this.store.kitsEnCola().length > 0 &&
    !this.store.guardando()
  );

  // ✅ FILTRO DE KITS
  readonly kitsFiltrados = computed(() => {
    const termino = this.busquedaKit().toLowerCase().trim();
    const disponibles = this.store.kitsDisponibles();
    if (!termino) return disponibles.slice(0, 50);
    return disponibles.filter(kit =>
      kit.serieMaestro?.toLowerCase().includes(termino) ||
      kit.serieSim?.toLowerCase().includes(termino) ||
      kit.serieDeco?.toLowerCase().includes(termino) ||
      kit.productoNombre?.toLowerCase().includes(termino) ||
      kit.modeloKitCodigo?.toLowerCase().includes(termino)
    ).slice(0, 50);
  });

  // ✅ EFECTO: Cargar kits cuando cambia la ZONA de origen
  private cargarKitsSiEsNecesario = effect(() => {
    // Para UI usamos zonaId filtrada, para lógica usamos zonaIdReal
    const zonaIdUI = this.zonaCtx.zonaId();
    const zonaIdReal = this.zonaCtx.zonaIdReal();

    // Si zonaIdUI es null (ADMIN/CONTADOR), usar zonaIdReal para cargar datos
    const zonaParaCargar = zonaIdUI ?? zonaIdReal;

    console.log('🔍 DEBUG effect - zonaIdUI:', zonaIdUI, 'zonaIdReal:', zonaIdReal, 'zonaParaCargar:', zonaParaCargar);

    if (zonaParaCargar && zonaParaCargar !== this.store.zonaKitsCargada()) {
      console.log('✅ Cargando kits para zona:', zonaParaCargar);
      this.store.cargarKitsDisponibles(zonaParaCargar);
    } else if (!zonaParaCargar) {
      console.warn('⏳ Esperando a que se cargue la zona del usuario...');
    }
  });

  ngOnInit(): void {
    this.store.cargarSucursales();
    this.store.limpiarCola();
  }

  // ── Scanner ───────────────────────────────────────────────────────────────
  onScannerKeydown(event: KeyboardEvent): void {
    const now = Date.now();
    if (event.key === 'Enter') {
      if (this.scanBuffer.trim()) this.procesarSerial(this.scanBuffer.trim());
      this.scanBuffer = '';
      this.lastKeyTime = 0;
      return;
    }
    if (now - this.lastKeyTime > this.SCANNER_THRESHOLD_MS && this.scanBuffer) {
      this.scanBuffer = '';
    }
    if (event.key.length === 1) this.scanBuffer += event.key;
    this.lastKeyTime = now;
  }

  onScannerInput(event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    this.valorScanner.set(val);
    this.store.limpiarErrorEscaneo();
  }

  buscarManual(): void {
    const val = this.valorScanner().trim();
    if (!val) return;
    this.procesarSerial(val);
    this.valorScanner.set('');
    this.scannerInput?.nativeElement.focus();
  }

  onEnterManual(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.buscarManual();
    }
  }

  private procesarSerial(serial: string): void {
    // Obtener zona: priorizar filtrada, fallback a real
    const zonaId = this.zonaCtx.zonaId() ?? this.zonaCtx.zonaIdReal();

    // 1. Buscar en lista cargada (más rápido)
    if (zonaId) {
      const kitEnLista = this.store.kitsDisponibles().find(k =>
        k.serieMaestro?.toLowerCase() === serial.toLowerCase() ||
        k.serieSim?.toLowerCase() === serial.toLowerCase() ||
        k.serieDeco?.toLowerCase() === serial.toLowerCase()
      );

      if (kitEnLista) {
        if (kitEnLista.estado === 'DISPONIBLE' && kitEnLista.zonaId === zonaId) {
          this.agregarKitDesdeSelector(kitEnLista);
          this.valorScanner.set('');
          return;
        } else {
          this.toastSvc.error(`El kit "${serial}" no está disponible para despacho`);
          this.valorScanner.set('');
          return;
        }
      }
    }

    // 2. Fallback: consultar backend
    this.store.escanearSerial(serial);
    setTimeout(() => {
      this.valorScanner.set('');
      this.scannerInput?.nativeElement.focus();
    }, 100);
  }

  // ── Acciones ──────────────────────────────────────────────────────────────
  quitarKit(itemKitId: number): void {
    this.store.quitarKitDeCola(itemKitId);
  }

  kitEstaEnCola(kitId: number): boolean {
    return this.store.kitsEnCola().some(k => k.itemKitId === kitId);
  }

  agregarKitDesdeSelector(kit: ItemKitResponse): void {
    if (this.kitEstaEnCola(kit.id)) {
      this.toastSvc.warning(`El kit "${kit.serieMaestro}" ya está en la lista`);
      return;
    }
    if (kit.estado !== 'DISPONIBLE') {
      this.toastSvc.error(`El kit "${kit.serieMaestro}" no está disponible`);
      return;
    }
    this.store.agregarKitALaCola(kit);
    this.busquedaKit.set('');
    this.toastSvc.success(`Kit "${kit.serieMaestro}" agregado`);
    this.scannerInput?.nativeElement.focus();
  }

  toggleSelector(): void {
    this.mostrarSelector.update(v => !v);
    if (!this.mostrarSelector()) this.busquedaKit.set('');
  }

  // ✅ MÉTODO ENVIAR - Validación por zona
  enviar(): void {
    const destId = this.sucursalDestinoId();
    const kits = this.store.kitsEnCola();

    // Obtener zona para validación
    const zonaId = this.zonaCtx.zonaId() ?? this.zonaCtx.zonaIdReal();

    if (!destId || !kits.length || !zonaId) {
      this.toastSvc.error('Selecciona destino y espera a que carguen los kits');
      return;
    }

    // Validación: kits deben pertenecer a la zona
    const kitsInvalidos = kits.filter(k => {
      const kitDisp = this.store.kitsDisponibles().find(kd => kd.id === k.itemKitId);
      return !kitDisp || kitDisp.estado !== 'DISPONIBLE' || kitDisp.zonaId !== zonaId;
    });

    if (kitsInvalidos.length > 0) {
      this.toastSvc.error(`${kitsInvalidos.length} kit(s) ya no están disponibles`);
      return;
    }

    // ✅ Determinar sucursal de origen:
    // - Si usuario tiene sucursal fija (ALMACENERO), usarla
    // - Si es ADMIN/JEFE, usar primera sucursal de la zona
    let origenId = this.zonaCtx.sucursalId();

    if (origenId === null && this.zonaCtx.tieneRol('ADMIN', 'JEFE_ALMACEN')) {
      const sucursalesDeZona = this.store.sucursales().filter(s => s.zonaId === zonaId);
      origenId = sucursalesDeZona[0]?.id ?? null;
    }

    if (!origenId) {
      this.toastSvc.error('No se pudo determinar la sucursal de origen');
      return;
    }

    this.store.crearDespacho({
      sucursalOrigenId: origenId,
      sucursalDestinoId: destId,
      itemKitIds: kits.map(k => k.itemKitId),
      guiaRemision: this.guiaRemision() || undefined,
      observaciones: this.observaciones() || undefined,
    });
  }

  volver(): void {
    this.router.navigate(['/logistica/pendientes']);
  }

  onSucursalChange(event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    this.sucursalDestinoId.set(val ? +val : null);
  }
}
