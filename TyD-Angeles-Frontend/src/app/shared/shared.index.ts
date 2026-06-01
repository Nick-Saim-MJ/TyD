// ── Layout ────────────────────────────────────────────────────
export { AppShellComponent }     from './components/layout/app-shell/app-shell.component';
export { SidebarComponent }      from './components/layout/sidebar/sidebar.component';
export { TopbarComponent }       from './components/layout/topbar/topbar.component';

// ── UI Components ─────────────────────────────────────────────
export { EstadoBadgeComponent }    from './components/ui/estado-badge/estado-badge.component';
export { ConfirmModalComponent }   from './components/ui/confirm-modal/confirm-modal.component';
export { ToastContainerComponent } from './components/ui/toast-container/toast-container.component';

// ── Directives ────────────────────────────────────────────────
export { HasRolDirective } from './directives/has-rol.directive';

// ── Pipes ─────────────────────────────────────────────────────
export { EstadoKitPipe }   from './pipes/pipes';
export { NombreZonaPipe }  from './pipes/pipes';
export { FechaLocalPipe }  from './pipes/pipes';

// ── Services ──────────────────────────────────────────────────
export { ToastService }       from './services/toast.service';
export { ModalService }       from './services/modal.service';
export { ZonaContextService } from './services/zona-context.service';
