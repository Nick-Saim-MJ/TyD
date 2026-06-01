// ─────────────────────────────────────────────────────────────────────────────
// ENUMS
// ─────────────────────────────────────────────────────────────────────────────

export type EstadoDespacho =
  | 'PREPARANDO' | 'EN_TRANSITO' | 'RECIBIDO'
  | 'RECIBIDO_CON_OBSERVACIONES' | 'CANCELADO';

export type EstadoDespachoItem =
  | 'ENVIADO' | 'RECIBIDO_OK' | 'RECIBIDO_DEFECTUOSO' | 'NO_RECIBIDO';

// ─────────────────────────────────────────────────────────────────────────────
// RESPONSE DTOs
// ─────────────────────────────────────────────────────────────────────────────

export interface DespachoItemResponse {
  id:                    number;
  itemKitId:             number;
  serieMaestro:          string;
  serieSim:              string;
  serieDeco:             string | null;
  productoNombre:        string;
  sucursalActualNombre:  string | null;
  estadoItem:            EstadoDespachoItem;
  observacion:           string | null;
}

export interface DespachoResponse {
  id:                      number;
  sucursalOrigenId:        number;
  sucursalOrigenNombre:    string;
  zonaOrigenId:            number;
  zonaOrigenNombre:        string;
  sucursalDestinoId:       number;
  sucursalDestinoNombre:   string;
  zonaDestinoId:           number;
  zonaDestinoNombre:       string;
  estado:                  EstadoDespacho;
  usuarioEnviaNombre:      string;
  usuarioRecibeNombre:     string | null;
  guiaRemision:            string | null;
  observaciones:           string | null;
  totalItems:              number;
  fechaDespacho:           string | null;
  fechaRecepcion:          string | null;
  createdAt:               string;
  esInterZona:             boolean;
}

export interface DespachoDetalleResponse extends DespachoResponse {
  items:             DespachoItemResponse[];
  totalEnviados:     number;
  totalRecibidosOk:  number;
  totalDefectuosos:  number;
  totalNoRecibidos:  number;
}

// ─────────────────────────────────────────────────────────────────────────────
// REQUEST DTOs
// ─────────────────────────────────────────────────────────────────────────────

export interface CrearDespachoRequest {
  sucursalOrigenId:  number;
  sucursalDestinoId: number;
  itemKitIds:        number[];
  guiaRemision?:     string;
  observaciones?:    string;
}

export interface ItemRecepcionRequest {
  itemKitId:         number;
  estadoRecepcion:   EstadoDespachoItem;
  observacion?:      string;
}

export interface ConfirmarRecepcionRequest {
  items:                  ItemRecepcionRequest[];
  observacionesGenerales?: string;
}

// ─────────────────────────────────────────────────────────────────────────────
// KIT EN COLA (para la UI de crear despacho)
// ─────────────────────────────────────────────────────────────────────────────

export interface KitEnCola {
  itemKitId:     number;
  serieMaestro:  string;
  serieSim:      string;
  serieDeco:     string | null;
  productoNombre: string;
  modeloCodigo:  string | null;
}

// ─────────────────────────────────────────────────────────────────────────────
// SUCURSAL (para los selectores de origen/destino)
// ─────────────────────────────────────────────────────────────────────────────

export interface SucursalOpcion {
  id:       number;
  nombre:   string;
  tipo:     'ALMACEN' | 'OFICINA';
  zonaId:   number;
  zonaNombre: string;
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

export const LABEL_ESTADO_DESPACHO: Record<EstadoDespacho, string> = {
  PREPARANDO:                   'Preparando',
  EN_TRANSITO:                  'En tránsito',
  RECIBIDO:                     'Recibido',
  RECIBIDO_CON_OBSERVACIONES:   'Recibido con observaciones',
  CANCELADO:                    'Cancelado',
};

export const LABEL_ESTADO_ITEM: Record<EstadoDespachoItem, string> = {
  ENVIADO:             'Enviado',
  RECIBIDO_OK:         'Recibido OK',
  RECIBIDO_DEFECTUOSO: 'Defectuoso',
  NO_RECIBIDO:         'No recibido',
};
