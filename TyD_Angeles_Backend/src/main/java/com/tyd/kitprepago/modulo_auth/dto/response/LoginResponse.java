package com.tyd.kitprepago.modulo_auth.dto.response;

import com.tyd.kitprepago.shared.security.Rol;

/**
 * Respuesta del login exitoso.
 * El frontend Angular guarda el token en localStorage y
 * lo incluye en cada request como: Authorization: Bearer <token>
 */
public record LoginResponse(
        String token,
        String tokenType,        // Siempre "Bearer"
        Long expiresIn,          // Milisegundos hasta expiración
        Long usuarioId,
        String username,
        String nombreCompleto,
        Rol rol,
        Long zonaId,
        String zonaNombre,       // Para mostrar en el header del frontend
        Long sucursalId,
        String sucursalNombre
) {
    public static final String BEARER = "Bearer";
}