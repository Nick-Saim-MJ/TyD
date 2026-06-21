import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { LogisticaStore } from '../../logistica.store';
import { LABEL_TIPO_EVENTO } from '../../models/logistica.model';
import { FechaLocalPipe } from '../../../../shared/pipes/pipes';

@Component({
  selector:        'app-mis-recepciones',
  standalone:      true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports:         [FechaLocalPipe],
  templateUrl:     './mis-recepciones.component.html',
  styleUrl:        './mis-recepciones.component.css',
})
export class MisRecepcionesComponent implements OnInit {

  readonly store  = inject(LogisticaStore);
  readonly router = inject(Router);

  readonly labelTipoEvento = LABEL_TIPO_EVENTO;

  ngOnInit(): void {
    this.store.cargarMisRecepciones();
  }

  volver(): void {
    this.router.navigate(['/logistica/pendientes']);
  }
}
