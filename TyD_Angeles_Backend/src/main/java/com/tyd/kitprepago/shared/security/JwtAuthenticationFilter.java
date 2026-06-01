package com.tyd.kitprepago.shared.security;

import com.tyd.kitprepago.shared.exception.CuentaBloqueadaException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Filtro JWT que se ejecuta UNA VEZ por request (OncePerRequestFilter).
 *
 * Flujo:
 *  1. Extrae el token del header Authorization: Bearer <token>
 *  2. Valida firma y expiración
 *  3. Verifica que la cuenta no esté bloqueada (bloqueado_hasta)
 *  4. Inyecta el UsuarioPrincipal en el SecurityContext
 *
 * Si algo falla, NO lanza excepción — simplemente no setea el contexto
 * y Spring Security rechazará el request con 401 automáticamente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", pasar al siguiente filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            // Solo procesar si hay username y no hay autenticación previa en el contexto
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UsuarioPrincipal principal = (UsuarioPrincipal) userDetailsService.loadUserByUsername(username);

                // Verificar bloqueo de cuenta por intentos fallidos
                // La entidad Usuario debe exponer getBloqueadoHasta()
                // Este check lo hace el UserDetailsServiceImpl al cargar el usuario
                // (isAccountNonLocked() retorna false si está bloqueado)
                if (!principal.isAccountNonLocked()) {
                    log.warn("Intento de acceso con cuenta bloqueada: {}", username);
                    // No seteamos el contexto → Spring rechaza con 401
                    filterChain.doFilter(request, response);
                    return;
                }

                if (jwtService.isTokenValid(jwt, principal)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Token malformado, expirado, firma inválida, etc.
            // Log para debugging pero NO interrumpir el filtro chain
            log.debug("JWT inválido o expirado: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
