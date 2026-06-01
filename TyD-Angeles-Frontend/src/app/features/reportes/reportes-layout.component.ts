// @ts-ignore
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule }           from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter, map }            from 'rxjs/operators';
import { toSignal }               from '@angular/core/rxjs-interop';
import { ReportesStore }          from './store/reportes.store';

interface Tab { label: string; ruta: string; icono: string; }

@Component({
  selector: 'app-reportes-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="reportes-shell">
      <!-- Tabs de navegación interna -->
      <nav class="reporte-tabs">
        <a *ngFor="let tab of tabs"
           [routerLink]="tab.ruta"
           class="reporte-tab"
           [class.reporte-tab--activo]="rutaActiva().includes(tab.ruta)">
          <span class="tab-icono">{{ tab.icono }}</span>
          {{ tab.label }}
        </a>
      </nav>

      <!-- Contenido del módulo -->
      <main class="reporte-content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .reportes-shell {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
      padding: 1.5rem 2rem;
      max-width: 1400px;
      margin: 0 auto;
    }

    .reporte-tabs {
      display: flex;
      gap: 0.25rem;
      border-bottom: 2px solid var(--color-gray-200);
      padding-bottom: 0;
    }

    .reporte-tab {
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      padding: 0.625rem 1.125rem;
      font-size: 0.875rem;
      font-weight: 600;
      color: var(--color-gray-500);
      text-decoration: none;
      border-radius: 6px 6px 0 0;
      border: 1px solid transparent;
      border-bottom: none;
      margin-bottom: -2px;
      transition: color 0.15s, background 0.15s;
    }

    .reporte-tab:hover {
      color: var(--color-primary);
      background: rgba(26, 58, 108, 0.05);
    }

    .reporte-tab--activo {
      color: var(--color-primary);
      background: var(--color-white);
      border-color: var(--color-gray-200);
      border-bottom-color: var(--color-white);
    }

    .tab-icono { font-size: 1rem; }

    .reporte-content { min-height: 400px; }
  `]
})
export class ReportesLayoutComponent {
  private readonly router = inject(Router);
  readonly store = inject(ReportesStore);

  readonly tabs: Tab[] = [
    { label: 'Ventas', ruta: 'ventas', icono: '🧾' },
    { label: 'Kardex', ruta: 'kardex', icono: '📦' },
  ];

  readonly rutaActiva = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map(e => (e as NavigationEnd).urlAfterRedirects)
    ),
    { initialValue: this.router.url }
  );
}
