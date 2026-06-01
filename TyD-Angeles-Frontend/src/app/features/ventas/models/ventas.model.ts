// ─────────────────────────────────────────────────────────────────────────────
// ENUMS
// ─────────────────────────────────────────────────────────────────────────────

export type TipoCliente   = 'PDV' | 'GENERAL';
export type CondicionVenta = 'CONTADO' | 'CREDITO';
export type EstadoVenta    = 'ACTIVA' | 'ANULADA';
export type MetodoPago     = 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TRANSFERENCIA';

export const METODOS_PAGO: { valor: MetodoPago; label: string; icono: string }[] = [
  { valor: 'EFECTIVO',      label: 'Efectivo',      icono: '💵' },
  { valor: 'YAPE',          label: 'Yape',           icono: '📱' },
  { valor: 'PLIN',          label: 'Plin',           icono: '📲' },
  { valor: 'TRANSFERENCIA', label: 'Transferencia',  icono: '🏦' },
];

// ─────────────────────────────────────────────────────────────────────────────
// RESPONSE DTOs
// ─────────────────────────────────────────────────────────────────────────────

export interface ClienteResponse {
  id:             number;
  dni:            string;
  nombres:        string;
  apellidos:      string;
  nombreCompleto: string;
  telefono:       string | null;
  tipo:           TipoCliente;
  razonSocial:    string | null;
  ruc:            string | null;
  createdAt:      string;
}

export interface VentaResponse {
  id:                    number;
  itemKitId:             number;
  serieMaestro:          string;
  serieSim:              string;
  productoNombre:        string;
  clienteId:             number;
  clienteNombreCompleto: string;
  clienteDni:            string;
  clienteTipo:           TipoCliente;
  vendedorId:            number;
  vendedorNombre:        string;
  sucursalVentaId:       number;
  sucursalVentaNombre:   string;
  zonaNombre:            string;
  montoVenta:            number;
  montoLiquidado:        number | null;
  condicion:             CondicionVenta;
  metodoPago:            string;
  estado:                EstadoVenta;
  motivoAnulacion:       string | null;
  liquidacionId:         number | null;
  fechaVenta:            string;
}

export interface UsuarioVendedor {
  id:             number;
  username:       string;
  nombreCompleto: string;
  rol:            string;
}

// ─────────────────────────────────────────────────────────────────────────────
// REQUEST DTOs
// ─────────────────────────────────────────────────────────────────────────────

export interface CrearClienteInlineRequest {
  dni:         string;
  nombres:     string;
  apellidos:   string;
  telefono?:   string;
  tipo:        TipoCliente;
  razonSocial?: string;
  ruc?:        string;
}

export interface RegistrarVentaRequest {
  itemKitId:          number;
  clienteDni:         string;
  nuevoCliente?:      CrearClienteInlineRequest;
  vendedorId:         number;
  sucursalVentaId:    number;
  montoVenta:         number;
  condicion:          CondicionVenta;
  metodoPago:         MetodoPago;
  montoRecargaInicial?: number;
}

export interface AnularVentaRequest {
  motivo: string;
}

export interface FiltroVentas {
  sucursalId?:  number | null;
  mes?:         string | null;  // YYYY-MM
  tipo?:        TipoCliente | null;
  vendedorId?:  number | null;
}

// ─────────────────────────────────────────────────────────────────────────────
// KIT VALIDADO (resultado del escaneo en el formulario)
// ─────────────────────────────────────────────────────────────────────────────

export type EstadoEscaneoKit =
  | 'IDLE'         // sin escanear
  | 'BUSCANDO'     // request en curso
  | 'DISPONIBLE'   // kit listo para vender
  | 'NO_DISPONIBLE' // kit en otro estado
  | 'NO_ENCONTRADO'; // serial inexistente

export interface KitValidado {
  id:             number;
  serieMaestro:   string;
  serieSim:       string;
  serieDeco:      string | null;
  productoNombre: string;
  modeloCodigo:   string | null;
  estado:         string;
  sucursalNombre: string | null;
}
