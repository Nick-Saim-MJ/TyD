// ─────────────────────────────────────────────────────────────────────────────
// ENUMS
// ─────────────────────────────────────────────────────────────────────────────

export type EstadoKit =
  | 'DISPONIBLE' | 'VENDIDO' | 'TRANSITO' | 'DEFECTUOSO' | 'DEVUELTO';

// ─────────────────────────────────────────────────────────────────────────────
// RESPONSE DTOs  (espejo exacto del backend)
// ─────────────────────────────────────────────────────────────────────────────

export interface ModeloKitResponse {
  id:          number;
  codigo:      string;         // LH100, LH300
  nombre:      string;
  descripcion: string | null;
  tieneDeco:   boolean;
  activo:      boolean;
}

export interface LoteResponse {
  id:                      number;
  numeroPedido:            string | null;
  numeroOperacion:         string | null;   // boucher — visible solo ADMIN/CONTADOR
  zonaId:                  number;
  zonaNombre:              string;
  zonaCodigoDirecTV:       string | null;
  sucursalRecepcionId:     number;
  sucursalRecepcionNombre: string;
  cantidadEsperada:        number;
  cantidadRecibida:        number;
  fechaPedido:             string | null;   // ISO date
  fechaRecepcion:          string | null;
  observaciones:           string | null;
  usuarioRegistroNombre:   string;
  createdAt:               string;
}

export interface ItemKitResponse {
  id:                    number;
  loteId:                number;
  numeroPedido:          string | null;
  numeroOperacion:       string | null;
  productoId:            number;
  productoNombre:        string;
  modeloKitId:           number | null;
  modeloKitCodigo:       string | null;
  tieneDeco:             boolean | null;
  serieMaestro:          string;
  serieSim:              string;
  serieDeco:             string | null;
  estado:                EstadoKit;
  sucursalActualId:      number | null;
  sucursalActualNombre:  string | null;
  zonaId:                number | null;
  zonaNombre:            string | null;
  custodioActualNombre:  string | null;
  fechaIngreso:          string;
}

// ─────────────────────────────────────────────────────────────────────────────
// FILTROS
// ─────────────────────────────────────────────────────────────────────────────

export interface FiltroLotes {
  zonaId?:  number | null;
  periodo?: string | null;    // YYYY-MM
}

// ─────────────────────────────────────────────────────────────────────────────
// AGRUPACIÓN por estado (para la vista de boucher)
// ─────────────────────────────────────────────────────────────────────────────

export interface GrupoEstado {
  estado: EstadoKit;
  items:  ItemKitResponse[];
}

export function agruparPorEstado(items: ItemKitResponse[]): GrupoEstado[] {
  const orden: EstadoKit[] = ['DISPONIBLE','VENDIDO','TRANSITO','DEFECTUOSO','DEVUELTO'];
  const map = new Map<EstadoKit, ItemKitResponse[]>();

  for (const item of items) {
    const arr = map.get(item.estado) ?? [];
    arr.push(item);
    map.set(item.estado, arr);
  }

  return orden
    .filter(e => map.has(e))
    .map(e => ({ estado: e, items: map.get(e)! }));
}
