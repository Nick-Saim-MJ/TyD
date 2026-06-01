import {
  Directive, inject, Input,
  TemplateRef, ViewContainerRef, OnInit
} from '@angular/core';

import { ZonaContextService } from '../services/zona-context.service';
import {Rol} from '../../models/auth.model';

/**
 * Directiva estructural que muestra u oculta elementos según el rol.
 *
 * Uso en templates:
 *   <button *appHasRol="['ADMIN', 'JEFE_ALMACEN']">Anular venta</button>
 *   <div *appHasRol="'CONTADOR'">Solo contabilidad</div>
 *
 * Si el usuario no tiene el rol indicado, el elemento no se renderiza (no
 * solo se oculta con CSS — se elimina del DOM completamente).
 */
@Directive({
  selector:   '[appHasRol]',
  standalone: true,
})
export class HasRolDirective implements OnInit {

  @Input('appHasRol') roles: Rol | Rol[] = [];

  private readonly tpl        = inject(TemplateRef<unknown>);
  private readonly vcr        = inject(ViewContainerRef);
  private readonly zonaCtx    = inject(ZonaContextService);

  ngOnInit(): void {
    const rolesRequeridos = Array.isArray(this.roles)
      ? this.roles
      : [this.roles];

    if (this.zonaCtx.tieneRol(...rolesRequeridos)) {
      this.vcr.createEmbeddedView(this.tpl);
    }
    // Si no tiene el rol → vcr queda vacío → elemento no existe en el DOM
  }
}
