import { Injectable } from '@angular/core';
import { LoginResponse, UsuarioSesion } from '../../models/auth.model';

const TOKEN_KEY = 'tyg_token';
const SESSION_KEY = 'tyg_session';

@Injectable({ providedIn: 'root' })
export class TokenService {
  guardarToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  obtenerToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  eliminarToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  tieneToken(): boolean {
    return !!this.obtenerToken();
  }

  estaExpirado(): boolean {
    const token = this.obtenerToken();
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  guardarSesion(login: LoginResponse): void {
    const sesion: UsuarioSesion = {
      id: login.usuarioId,
      username: login.username,
      nombreCompleto: login.nombreCompleto,
      rol: login.rol,
      zonaId: login.zonaId,
      zonaNombre: login.zonaNombre,
      sucursalId: login.sucursalId,
      sucursalNombre: login.sucursalNombre,
      permisos: [],
    };
    localStorage.setItem(SESSION_KEY, JSON.stringify(sesion));
  }

  actualizarPermisos(permisos: string[]): void {
    const sesion = this.obtenerSesion();
    if (sesion) {
      sesion.permisos = permisos;
      localStorage.setItem(SESSION_KEY, JSON.stringify(sesion));
    }
  }

  obtenerSesion(): UsuarioSesion | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UsuarioSesion;
    } catch {
      return null;
    }
  }

  limpiar(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(SESSION_KEY);
  }
}
