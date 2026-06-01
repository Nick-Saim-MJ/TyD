import {
  ChangeDetectionStrategy, Component,
  DestroyRef, ElementRef, OnInit,
  ViewChild, computed, effect, inject, signal
} from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { VentasStore } from '../../ventas.store';
import { ZonaContextService } from '../../../../shared/services/zona-context.service';
import { ToastService } from '../../../../shared/services/toast.service';
import {
  ClienteResponse, CondicionVenta, CrearClienteInlineRequest,
  METODOS_PAGO, MetodoPago, TipoCliente
} from '../../models/ventas.model';

@Component({
  selector:        'app-registrar-venta',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [FormsModule, ReactiveFormsModule],
  templateUrl:     './registrar-venta.component.html',
  styleUrl:        './registrar-venta.component.css',
})
export class RegistrarVentaComponent implements OnInit {

  @ViewChild('kitInput') kitInput!: ElementRef<HTMLInputElement>;

  readonly store      = inject(VentasStore);
  readonly zonaCtx    = inject(ZonaContextService);
  readonly router     = inject(Router);
  readonly toastSvc   = inject(ToastService);
  readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  // ── Opciones del formulario ───────────────────────────────────────────────
  readonly metodosPago  = METODOS_PAGO;
  readonly tiposCliente: { valor: TipoCliente; label: string }[] = [
    { valor: 'GENERAL', label: 'Persona natural (DNI)' },
    { valor: 'PDV',     label: 'Punto de venta (RUC)' },
  ];
  readonly esUsuarioActual = computed(() => {
    const rol = this.zonaCtx.rol();
    return rol === 'VENDEDOR' || rol === 'ALMACENERO';
  });

  // ── Computed ──────────────────────────────────────────────────────────────
  readonly vendedorActualNombre = computed(() => {
    const userId = this.zonaCtx.usuarioId();
    if (!userId) return null;
    const vendedor = this.store.vendedores().find(v => v.id === userId);
    return vendedor?.nombreCompleto ?? null;
  });

  readonly vendedorActualRol = computed(() => {
    const userId = this.zonaCtx.usuarioId();
    if (!userId) return null;
    const vendedor = this.store.vendedores().find(v => v.id === userId);
    return vendedor?.rol ?? null;
  });

  // ── Estado local ─────────────────────────────────────────────────────────
  readonly valorKit          = signal<string>('');
  readonly valorBusqCliente  = signal<string>('');
  readonly mostrarFormNuevo  = signal<boolean>(false);
  readonly metodoPago        = signal<MetodoPago>('EFECTIVO');
  readonly condicion         = signal<CondicionVenta>('CONTADO');
  readonly montoVenta        = signal<number | null>(null);
  readonly montoRecarga      = signal<number | null>(null);
  readonly vendedorId        = signal<number | null>(null);
  readonly dropdownAbierto   = signal<boolean>(false);

  // ── Formulario de nuevo cliente (inline) ─────────────────────────────────
  readonly formNuevoCliente = this.fb.group({
    dni:         ['', [Validators.required, Validators.minLength(8)]],
    nombres:     ['', Validators.required],
    apellidos:   ['', Validators.required],
    telefono:    [''],
    tipo:        ['GENERAL' as TipoCliente],
    razonSocial: [''],
    ruc:         [''],
  });

  // ── Computed ──────────────────────────────────────────────────────────────
  readonly puedeRegistrar = computed(() =>
    !!this.store.kitValidado() &&
    (!!this.store.clienteSeleccionado() || this.mostrarFormNuevo()) &&
    !!this.montoVenta() &&
    !this.store.guardando()
  );

  readonly sucursalVentaId = computed(() =>
    this.zonaCtx.sucursalId() ?? 0
  );

  readonly esPDV = computed(() =>
    this.formNuevoCliente.value.tipo === 'PDV'
  );

  // Scanner
  private lastKeyTime = 0;
  private scanBuffer  = '';
  private readonly SCANNER_MS = 50;

  ngOnInit(): void {
    const zonaId = this.zonaCtx.zonaId();
    if (zonaId) this.store.cargarVendedores(zonaId);

    const rol = this.zonaCtx.rol();
    const usuarioId = this.zonaCtx.usuarioId();

    if (usuarioId && (rol === 'VENDEDOR' || rol === 'ALMACENERO')) {
      this.vendedorId.set(usuarioId);
    }

    // Resetear estado de escaneo al entrar
    this.store.limpiarKit();
    this.store.limpiarCliente();
    this.store.limpiarVentaExito();
  }

  // ── Scanner ───────────────────────────────────────────────────────────────
  onKitKeydown(event: KeyboardEvent): void {
    const now = Date.now();
    if (event.key === 'Enter') {
      const val = this.scanBuffer.trim() || this.valorKit().trim();
      if (val) this.procesarKit(val);
      this.scanBuffer = '';
      this.lastKeyTime = 0;
      return;
    }
    if (now - this.lastKeyTime > this.SCANNER_MS && this.scanBuffer) {
      this.scanBuffer = '';
    }
    if (event.key.length === 1) this.scanBuffer += event.key;
    this.lastKeyTime = now;
  }

  onKitInput(event: Event): void {
    this.valorKit.set((event.target as HTMLInputElement).value);
    if (this.store.kitValidado()) this.store.limpiarKit();
  }

  buscarKitManual(): void {
    const v = this.valorKit().trim();
    if (v) this.procesarKit(v);
  }

  private procesarKit(serial: string): void {
    this.store.escanearKit(serial);
    setTimeout(() => {
      this.valorKit.set('');
      this.kitInput?.nativeElement.focus();
    }, 150);
  }

  // ── Autocomplete cliente ──────────────────────────────────────────────────
  onBuscarCliente(event: Event): void {
    const q = (event.target as HTMLInputElement).value;
    this.valorBusqCliente.set(q);
    this.dropdownAbierto.set(true);
    if (this.store.clienteSeleccionado()) this.store.limpiarCliente();
    this.store.buscarClientes(q);
  }

  seleccionarCliente(cliente: ClienteResponse): void {
    this.store.seleccionarCliente(cliente);
    this.valorBusqCliente.set(cliente.nombreCompleto);
    this.dropdownAbierto.set(false);
    this.mostrarFormNuevo.set(false);
  }

  toggleNuevoCliente(): void {
    this.mostrarFormNuevo.update(v => !v);
    if (this.mostrarFormNuevo()) {
      // Pre-rellenar el DNI si el usuario ya escribió algo
      const q = this.valorBusqCliente().trim();
      if (q.length >= 8 && /^\d+$/.test(q)) {
        this.formNuevoCliente.patchValue({ dni: q });
      }
      this.store.limpiarCliente();
    }
  }

  // ── Enviar venta ──────────────────────────────────────────────────────────
  registrar(): void {
    const kit = this.store.kitValidado();
    if (!kit) return;

    const cliente  = this.store.clienteSeleccionado();
    const esNuevo  = this.mostrarFormNuevo();

    if (esNuevo && this.formNuevoCliente.invalid) {
      this.formNuevoCliente.markAllAsTouched();
      return;
    }

    const dniCliente = esNuevo
      ? this.formNuevoCliente.value.dni!
      : cliente!.dni;

    const nuevoCliente: CrearClienteInlineRequest | undefined = esNuevo ? {
      dni:         this.formNuevoCliente.value.dni!,
      nombres:     this.formNuevoCliente.value.nombres!,
      apellidos:   this.formNuevoCliente.value.apellidos!,
      telefono:    this.formNuevoCliente.value.telefono || undefined,
      tipo:        this.formNuevoCliente.value.tipo as TipoCliente,
      razonSocial: this.formNuevoCliente.value.razonSocial || undefined,
      ruc:         this.formNuevoCliente.value.ruc || undefined,
    } : undefined;

    this.store.registrarVenta({
      itemKitId:          kit.id,
      clienteDni:         dniCliente,
      nuevoCliente,
      vendedorId:         this.vendedorId() ?? 0,
      sucursalVentaId:    this.sucursalVentaId(),
      montoVenta:         this.montoVenta()!,
      condicion:          this.condicion(),
      metodoPago:         this.metodoPago(),
      montoRecargaInicial: this.montoRecarga() ?? undefined,
    });
  }

  nuevaVenta(): void {
    this.store.limpiarKit();
    this.store.limpiarCliente();
    this.store.limpiarVentaExito();
    this.valorKit.set('');
    this.valorBusqCliente.set('');
    this.mostrarFormNuevo.set(false);
    this.montoVenta.set(null);
    this.montoRecarga.set(null);
    this.formNuevoCliente.reset({ tipo: 'GENERAL' });
    setTimeout(() => this.kitInput?.nativeElement.focus(), 100);
  }

  irHistorial(): void {
    this.router.navigate(['/ventas/historial']);
  }

  onVendedorChange(event: Event): void {
    const v = (event.target as HTMLSelectElement).value;
    this.vendedorId.set(v ? +v : null);
  }

  onMetodoPagoSelect(metodo: MetodoPago): void {
    this.metodoPago.set(metodo);
  }
}
