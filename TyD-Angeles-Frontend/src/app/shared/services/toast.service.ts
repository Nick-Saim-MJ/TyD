import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  type: ToastType;
  titulo: string;
  mensaje?: string; // Opcional, según tu @if (toast.mensaje)
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  // Signal privada con la lista de notificaciones activas
  private readonly _toasts = signal<Toast[]>([]);

  // Exposición pública de solo lectura que consume tu toastSvc.toasts() en el HTML
  readonly toasts = this._toasts.asReadonly();

  private nextId = 1;

  /**
   * Muestra un toast de Éxito
   */
  success(titulo: string, mensaje?: string, duration = 4000): void {
    this.crear(titulo, mensaje, 'success', duration);
  }

  /**
   * Muestra un toast de Error (damos un poco más de tiempo para leer fallos complejos)
   */
  error(titulo: string, mensaje?: string, duration = 5000): void {
    this.crear(titulo, mensaje, 'error', duration);
  }

  /**
   * Muestra un toast de Advertencia
   */
  warning(titulo: string, mensaje?: string, duration = 4000): void {
    this.crear(titulo, mensaje, 'warning', duration);
  }

  /**
   * Muestra un toast de Información / Por defecto
   */
  info(titulo: string, mensaje?: string, duration = 4000): void {
    this.crear(titulo, mensaje, 'info', duration);
  }

  /**
   * Elimina un toast de la lista por su ID.
   * Invocado manualmente por el botón de cierre del HTML o automáticamente por el temporizador.
   */
  cerrar(id: number): void {
    this._toasts.update(toasts => toasts.filter(t => t.id !== id));
  }

  /**
   * Lógica interna centralizada para instanciar y programar la autodestrucción del toast
   */
  private crear(titulo: string, mensaje: string | undefined, type: ToastType, duration: number): void {
    const id = this.nextId++;
    const nuevoToast: Toast = { id, titulo, mensaje, type, duration };

    // Agregamos el nuevo toast al estado de la signal
    this._toasts.update(toasts => [...toasts, nuevoToast]);

    // Si la duración es mayor a 0, programamos su salida automática
    if (duration > 0) {
      setTimeout(() => this.cerrar(id), duration);
    }
  }
}
