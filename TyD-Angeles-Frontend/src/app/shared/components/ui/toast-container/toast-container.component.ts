import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector:        'app-toast-container',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl:     './toast-container.component.html',
  styleUrl:        './toast-container.component.css',
})
export class ToastContainerComponent {
  readonly toastSvc = inject(ToastService);

  cerrar(id: number): void {
    this.toastSvc.cerrar(id);
  }

  trackById(_: number, t: { id: number }): number {
    return t.id;
  }
}
