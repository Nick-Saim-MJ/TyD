export type Rol = 'ADMIN' | 'JEFE_ALMACEN' | 'ALMACENERO' | 'VENDEDOR' | 'CONTADOR';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: 'Bearer';
  expiresIn: number;
  usuarioId: number;
  username: string;
  nombreCompleto: string;
  rol: Rol;
  zonaId: number | null;
  zonaNombre: string | null;
  sucursalId: number | null;
  sucursalNombre: string | null;
}

export interface MeResponse {
  id: number;
  username: string;
  nombreCompleto: string;
  email: string | null;
  rol: Rol;
  zonaId: number | null;
  zonaNombre: string | null;
  zonaCodigoDirecTV: string | null;
  sucursalId: number | null;
  sucursalNombre: string | null;
  ultimoLogin: string | null;
  permisos: string[];
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  path: string;
  detalles?: Record<string, unknown>;
}

export interface UsuarioSesion {
  id: number;
  username: string;
  nombreCompleto: string;
  rol: Rol;
  zonaId: number | null;
  zonaNombre: string | null;
  sucursalId: number | null;
  sucursalNombre: string | null;
  permisos: string[];
}

export const RUTA_POR_ROL: Record<Rol, string> = {
  ADMIN: '/dashboard',
  JEFE_ALMACEN: '/inventario',
  ALMACENERO: '/inventario',
  VENDEDOR: '/ventas',
  CONTADOR: '/reportes',
};
