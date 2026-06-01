import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import {EstadoKitPipe} from '../../../pipes/pipes';


export type BadgeVariant =
  | 'success' | 'error' | 'warning'
  | 'info'    | 'neutral' | 'orange';

const ESTADO_VARIANT: Record<string, BadgeVariant> = {
  DISPONIBLE:  'success',
  VENDIDO:     'neutral',
  TRANSITO:    'warning',
  DEFECTUOSO:  'error',
  DEVUELTO:    'info',
  ACTIVO:      'success',
  PENDIENTE:   'warning',
  ANULADA:     'error',
  APROBADO:    'success',
  RECHAZADO:   'error',
  OBSERVADO:   'orange',
  EN_TRANSITO: 'warning',
  RECIBIDO:    'success',
  RECIBIDO_CON_OBSERVACIONES: 'orange',
  CANCELADO:   'error',
};

/**
 * Badge de estado reutilizable para kits, despachos, ventas, liquidaciones.
 *
 * Uso:
 *   <app-estado-badge estado="DISPONIBLE" />
 *   <app-estado-badge estado="VENDIDO" />
 *   <app-estado-badge estado="PENDIENTE" [custom]="'Mi texto'" />
 */
@Component({
  selector:        'app-estado-badge',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [EstadoKitPipe],
  templateUrl:     './estado-badge.component.html',
  styleUrl:        './estado-badge.component.css',
})
export class EstadoBadgeComponent {

  @Input({ required: true }) estado!: string;
  /** Sobreescribe el texto mostrado (por defecto usa EstadoKitPipe) */
  @Input() custom?: string;

  get variant(): BadgeVariant {
    return ESTADO_VARIANT[this.estado] ?? 'neutral';
  }
}
