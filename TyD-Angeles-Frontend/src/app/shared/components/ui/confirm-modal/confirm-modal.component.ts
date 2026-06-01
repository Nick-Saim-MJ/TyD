import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import {ModalService} from '../../../shared.index';

@Component({
  selector:        'app-confirm-modal',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl:     './confirm-modal.component.html',
  styleUrl:        './confirm-modal.component.css',
})
export class ConfirmModalComponent {
  readonly modal = inject(ModalService);

  confirmar(): void { this.modal.responder(true);  }
  cancelar():  void { this.modal.responder(false); }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.cancelar();
    }
  }
}
