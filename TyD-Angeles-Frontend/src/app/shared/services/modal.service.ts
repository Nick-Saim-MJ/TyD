import { Injectable, signal } from '@angular/core';
import { Subject } from 'rxjs';

export type ModalTipo = 'info' | 'warning' | 'danger';

export interface ModalState {
  visible: boolean;
  tipo: ModalTipo;
  titulo: string;
  mensaje: string;
  labelConfirmar: string;
  labelCancelar: string;
}

export interface AbrirModalConfig {
  titulo: string;
  mensaje: string;
  tipo?: ModalTipo; // Opcional, por defecto 'info'
  labelConfirmar?: string; // Opcional, por defecto 'Aceptar'
  labelCancelar?: string; // Opcional, por defecto 'Cancelar'
}

@Injectable({ providedIn: 'root' })
export class ModalService {

  // Estado inicial por defecto para evitar errores de lectura al iniciar la app
  private readonly estadoInicial: ModalState = {
    visible: false,
    tipo: 'info',
    titulo: '',
    mensaje: '',
    labelConfirmar: 'Aceptar',
    labelCancelar: 'Cancelar'
  };

  // Signal privada que muta internamente
  private readonly _state = signal<ModalState>(this.estadoInicial);

  // Exposición pública como propiedad de solo lectura.
  // En tu HTML lo invocas como modal.state()
  readonly state = this._state.asReadonly();

  // El puente RxJS para resolver la promesa asíncronamente
  private respuesta$?: Subject<boolean>;

  /**
   * Abre el modal de confirmación y retorna una Promesa que resuelve en true o false.
   *
   * Uso:
   * const seguro = await this.modalService.confirmar({
   *   titulo: '¿Eliminar registro?',
   *   mensaje: 'Esta acción no se puede deshacer.',
   *   tipo: 'danger'
   * });
   */
  confirmar(config: AbrirModalConfig): Promise<boolean> {
    // Seteamos el estado unificado mezclando los valores por defecto con lo que envíe el usuario
    this._state.set({
      visible: true,
      tipo: config.tipo ?? 'info',
      titulo: config.titulo,
      mensaje: config.mensaje,
      labelConfirmar: config.labelConfirmar ?? 'Aceptar',
      labelCancelar: config.labelCancelar ?? 'Cancelar'
    });

    this.respuesta$ = new Subject<boolean>();

    return new Promise<boolean>((resolve) => {
      // Nos suscribimos una única vez. Cuando se resuelva, limpiamos todo.
      this.respuesta$?.subscribe((resultado) => {
        this.resetearEstado();
        resolve(resultado);
      });
    });
  }

  /**
   * Responde al modal activo. Invocado por ConfirmModalComponent (confirmar / cancelar)
   */
  responder(respuesta: boolean): void {
    if (this.respuesta$) {
      this.respuesta$.next(respuesta);
      this.respuesta$.complete();
    } else {
      this.resetearEstado();
    }
  }

  private resetearEstado(): void {
    this._state.set(this.estadoInicial);
    this.respuesta$ = undefined;
  }
}
