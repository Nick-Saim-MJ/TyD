import { Pipe, PipeTransform } from '@angular/core';

// ─────────────────────────────────────────────────────────────────────────────
// ESTADO KIT PIPE
// Convierte 'DISPONIBLE' → 'Disponible', 'EN_TRANSITO' → 'En tránsito', etc.
// ─────────────────────────────────────────────────────────────────────────────

const ESTADOS: Record<string, string> = {
  DISPONIBLE:  'Disponible',
  VENDIDO:     'Vendido',
  TRANSITO:    'En tránsito',
  DEFECTUOSO:  'Defectuoso',
  DEVUELTO:    'Devuelto',
};

@Pipe({ name: 'estadoKit', standalone: true, pure: true })
export class EstadoKitPipe implements PipeTransform {
  transform(estado: string | null | undefined): string {
    if (!estado) return '—';
    return ESTADOS[estado] ?? estado;
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// NOMBRE ZONA PIPE
// Muestra nombre corto de la zona: 'Puno', 'Cusco', etc.
// ─────────────────────────────────────────────────────────────────────────────

@Pipe({ name: 'nombreZona', standalone: true, pure: true })
export class NombreZonaPipe implements PipeTransform {
  transform(nombre: string | null | undefined): string {
    return nombre ?? 'Sin zona';
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// FECHA LOCAL PIPE
// Convierte ISO string a formato legible en español (Perú)
// ─────────────────────────────────────────────────────────────────────────────

@Pipe({ name: 'fechaLocal', standalone: true, pure: true })
export class FechaLocalPipe implements PipeTransform {

  transform(
    fecha: string | Date | null | undefined,
    formato: 'fecha' | 'fecha-hora' | 'hora' | 'relativa' = 'fecha'
  ): string {
    if (!fecha) return '—';

    const d = typeof fecha === 'string' ? new Date(fecha) : fecha;
    if (isNaN(d.getTime())) return '—';

    const locale = 'es-PE';

    switch (formato) {
      case 'fecha':
        return d.toLocaleDateString(locale, {
          day: '2-digit', month: '2-digit', year: 'numeric'
        });
      case 'fecha-hora':
        return d.toLocaleString(locale, {
          day: '2-digit', month: '2-digit', year: 'numeric',
          hour: '2-digit', minute: '2-digit'
        });
      case 'hora':
        return d.toLocaleTimeString(locale, {
          hour: '2-digit', minute: '2-digit'
        });
      case 'relativa':
        return this.relativa(d);
      default:
        return d.toLocaleDateString(locale);
    }
  }

  private relativa(d: Date): string {
    const diff  = Date.now() - d.getTime();
    const mins  = Math.floor(diff / 60_000);
    const hours = Math.floor(diff / 3_600_000);
    const days  = Math.floor(diff / 86_400_000);

    if (mins  <  1) return 'Hace un momento';
    if (mins  < 60) return `Hace ${mins} min`;
    if (hours < 24) return `Hace ${hours} h`;
    if (days  <  7) return `Hace ${days} días`;

    return d.toLocaleDateString('es-PE', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }
}
