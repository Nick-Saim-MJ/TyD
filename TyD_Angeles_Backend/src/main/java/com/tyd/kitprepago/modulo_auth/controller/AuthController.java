package com.tyd.kitprepago.modulo_auth.controller;

import com.tyd.kitprepago.modulo_auth.dto.request.*;
import com.tyd.kitprepago.modulo_auth.dto.response.*;
import com.tyd.kitprepago.modulo_auth.service.AuthService;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ═══════════════════════════════════════════════════════════════════════════
// AUTH CONTROLLER
// ═══════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Público — SecurityConfig tiene .permitAll() para este endpoint.
     * Flujo: validar credenciales → verificar bloqueo → generar JWT
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/logout
     * Invalida el token en la blacklist en memoria.
     * El header Authorization es obligatorio (ya que el usuario debe estar autenticado).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.noContent().build(); // 204
    }

    /**
     * GET /api/auth/me
     * Devuelve el perfil completo del usuario autenticado con su lista de permisos.
     * Angular lo usa para construir el menú lateral y ocultar/mostrar botones.
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal));
    }
}

