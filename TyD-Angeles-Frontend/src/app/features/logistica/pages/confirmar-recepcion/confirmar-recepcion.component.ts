import {
  ChangeDetectionStrategy, Component,
  OnDestroy, OnInit, computed, inject, signal
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LogisticaStore } from '../../logistica.store';
import { EstadoBadgeComponent } from '../../../../shared/components/ui/estado-badge/estado-badge.component';
import { FechaLocalPipe } from '../../../../shared/pipes/pipes';
import {
  ConfirmarRecepcionRequest, EstadoDespachoItem,
  ItemRecepcionRequest
} from '../../models/logistica.model';

/** Estado de confirmación por kit en la vista */
interface KitConfirmacion {
  itemKitId:        number;
  serieMaestro:     string;
  serieSim:         string;
  serieDeco:        string | null;
  productoNombre:   string;
  estadoAnterior:   EstadoDespachoItem;
  estadoElegido:    EstadoDespachoItem;
  observacion:      string;
  observacionAbierta: boolean;
}

@Component({
  selector:        'app-confirmar-recepcion',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [FormsModule, EstadoBadgeComponent, FechaLocalPipe],
  templateUrl:     './confirmar-recepcion.component.html',
  styleUrl:        './confirmar-recepcion.component.css',
})

export class ConfirmarRecepcionComponent implements OnInit, OnDestroy {

  private readonly route  = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly store          = inject(LogisticaStore);

  // ── Confirmaciones: un estado por kit ────────────────────────────────────
  readonly confirmaciones = signal<KitConfirmacion[]>([]);

  // ── Observación general ───────────────────────────────────────────────────
  readonly observacionGeneral = signal<string>('');

  // ── Resumen calculado ─────────────────────────────────────────────────────
  readonly resumen = computed(() => {
    const c = this.confirmaciones();
    return {
      total:       c.length,
      ok:          c.filter(k => k.estadoElegido === 'RECIBIDO_OK').length,
      defectuoso:  c.filter(k => k.estadoElegido === 'RECIBIDO_DEFECTUOSO').length,
      noRecibido:  c.filter(k => k.estadoElegido === 'NO_RECIBIDO').length,
      sinDecision: c.filter(k => k.estadoElegido === 'ENVIADO').length,
    };
  });

  readonly todoDecidido = computed(() =>
    this.confirmaciones().every(k => k.estadoElegido !== 'ENVIADO')
  );

  readonly puedeEnviar = computed(() =>
    this.todoDecidido() && !this.store.guardando()
  );

  // ── Ciclo de vida ─────────────────────────────────────────────────────────
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.store.cargarDetalle(id);
    }
    // Cuando cargue el detalle, inicializar confirmaciones
    this.observarDetalle();
  }

  private observarDetalle(): void {
    // Poll simple: cuando el despachoActual cargue, inicializar la lista
    const intervalo = setInterval(() => {
      const d = this.store.despachoActual();
      if (d) {
        clearInterval(intervalo);
        this.confirmaciones.set(
          d.items.map(item => ({
            itemKitId:          item.itemKitId,
            serieMaestro:       item.serieMaestro,
            serieSim:           item.serieSim,
            serieDeco:          item.serieDeco,
            productoNombre:     item.productoNombre,
            estadoAnterior:     item.estadoItem,
            estadoElegido:      'ENVIADO' as EstadoDespachoItem,
            observacion:        '',
            observacionAbierta: false,
          }))
        );
      }
    }, 200);
  }

  ngOnDestroy(): void {
    this.store.limpiarDetalle();
  }

  // ── Acciones por kit ──────────────────────────────────────────────────────
  setEstado(itemKitId: number, estado: EstadoDespachoItem): void {
    this.confirmaciones.update(lista =>
      lista.map(k => {
        if (k.itemKitId !== itemKitId) return k;
        return {
          ...k,
          estadoElegido:      estado,
          // Abrir campo de observación automáticamente si es defectuoso
          observacionAbierta: estado === 'RECIBIDO_DEFECTUOSO',
        };
      })
    );
  }

  toggleObservacion(itemKitId: number): void {
    this.confirmaciones.update(lista =>
      lista.map(k =>
        k.itemKitId === itemKitId
          ? { ...k, observacionAbierta: !k.observacionAbierta }
          : k
      )
    );
  }

  setObservacion(itemKitId: number, texto: string): void {
    this.confirmaciones.update(lista =>
      lista.map(k =>
        k.itemKitId === itemKitId ? { ...k, observacion: texto } : k
      )
    );
  }

  /** Marcar todos los kits que no han sido decididos como RECIBIDO_OK */
  marcarTodosOk(): void {
    this.confirmaciones.update(lista =>
      lista.map(k =>
        k.estadoElegido === 'ENVIADO'
          ? { ...k, estadoElegido: 'RECIBIDO_OK' as EstadoDespachoItem, observacionAbierta: false }
          : k
      )
    );
  }

  // ── Enviar confirmación ───────────────────────────────────────────────────
  confirmar(): void {
    const despachoId = this.store.despachoActual()?.id;
    if (!despachoId) return;

    const items: ItemRecepcionRequest[] = this.confirmaciones().map(k => ({
      itemKitId:       k.itemKitId,
      estadoRecepcion: k.estadoElegido,
      observacion:     k.observacion || undefined,
    }));

    const req: ConfirmarRecepcionRequest = {
      items,
      observacionesGenerales: this.observacionGeneral() || undefined,
    };

    this.store.confirmarRecepcion(despachoId, req);
  }

  volver(): void {
    this.router.navigate(['/logistica/pendientes']);
  }

  trackById(_: number, k: KitConfirmacion): number {
    return k.itemKitId;
  }
}
