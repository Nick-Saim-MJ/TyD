// ─── Filtros ───────────────────────────────────────────────────────────────
export interface FiltrosVentas {
  zonaId:     number | null;
  sucursalId: number | null;
  vendedorId: number | null;
  mes:        string | null;   // 'YYYY-MM'
  page:       number;
  size:       number;
}

export interface FiltrosKardex {
  sucursalId:  number | null;
  modeloKit:   'LH100' | 'LH300' | null;
  periodo:     string | null;
}

// ─── Catálogos ─────────────────────────────────────────────────────────────
export interface ZonaOption     { id: number; nombre: string; }
export interface SucursalOption { id: number; nombre: string; zonaId: number; tipo: 'ALMACEN' | 'OFICINA'; }
export interface VendedorOption { id: number; nombres: string; sucursalId: number; }

// ─── Tabla de Ventas (alineado al Excel antiguo) ──────────────────────────
export interface VentaReporte {
  ventaId:         number;
  zona:            string;
  sucursal:        string;
  serieMaestro:    string;
  serieSim:        string;
  producto:        string;
  clienteDni:      string;
  clienteNombre:   string;
  clienteTipo:     'GENERAL' | 'PDV' | null;
  vendedor:        string;
  monto:           number;
  condicion:       'CONTADO' | 'CREDITO' | null;
  metodoPago:      string | null;
  estado:          'ACTIVA' | 'ANULADA' | 'PENDIENTE' | null;
  montoLiquidado:  number | null;
  fechaVenta:      string; // ISO date
}

export interface PagedResponse<T> {
  content:       T[];
  totalElements: number;
  totalPages:    number;
  number:        number;
  size:          number;
}

// ─── Kardex (sin cambios) ──────────────────────────────────────────────────
export interface KardexResumen {
  id:             number;
  sucursalId:     number;
  sucursalNombre: string;
  zonaNombre:     string;
  modeloKit:      'LH100' | 'LH300';
  periodo:        string;
  stockInicio:    number;
  entradas:       number;
  salidas:        number;
  stockFin:       number;
  cerrado:        boolean;
  fechaCierre:    string | null;
}

export interface KardexDetalle {
  fecha:          string;
  tipoMovimiento: 'ENTRADA' | 'SALIDA' | 'AJUSTE';
  concepto:       string;
  cantidad:       number;
  stockAcum:      number;
  referencia:     string;
}
