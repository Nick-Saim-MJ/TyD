package com.tyd.kitprepago.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones.
 * Todas las respuestas de error usan ApiErrorResponse para que Angular
 * siempre sepa cómo parsear un error independientemente del tipo.
 *
 * JSON de ejemplo:
 * {
 *   "timestamp": "2025-03-01T10:30:00Z",
 *   "status": 409,
 *   "error": "CONFLICT",
 *   "mensaje": "El kit K10RBA50E0093 no está disponible. Estado actual: VENDIDO",
 *   "path": "/api/ventas",
 *   "detalles": { "serieMaestro": "K10RBA50E0093", "estadoActual": "VENDIDO" }
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── SEGURIDAD ─────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {
        // Mensaje genérico: no revelar si el usuario no existe o la contraseña es incorrecta
        return error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", req, null);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ApiErrorResponse> handleCuentaBloqueada(
            CuentaBloqueadaException ex, HttpServletRequest req) {
        Map<String, Object> detalles = Map.of(
                "bloqueadoHasta", ex.getBloqueadoHasta().toString()
        );
        return error(HttpStatus.LOCKED, ex.getMessage(), req, detalles);
    }

    @ExceptionHandler({LockedException.class})
    public ResponseEntity<ApiErrorResponse> handleLocked(
            LockedException ex, HttpServletRequest req) {
        return error(HttpStatus.LOCKED, "Cuenta bloqueada temporalmente", req, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabled(
            DisabledException ex, HttpServletRequest req) {
        return error(HttpStatus.UNAUTHORIZED,
                "Cuenta inactiva. Contacta al administrador", req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN,
                "No tienes permisos para esta acción", req, null);
    }

    @ExceptionHandler(ZonaNoAutorizadaException.class)
    public ResponseEntity<ApiErrorResponse> handleZonaNoAutorizada(
            ZonaNoAutorizadaException ex, HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
    }

    // ── INVENTARIO ────────────────────────────────────────────

    @ExceptionHandler(KitNoDisponibleException.class)
    public ResponseEntity<ApiErrorResponse> handleKitNoDisponible(
            KitNoDisponibleException ex, HttpServletRequest req) {
        Map<String, Object> detalles = Map.of(
                "serieMaestro", ex.getSerieMaestro(),
                "estadoActual", ex.getEstadoActual()
        );
        return error(HttpStatus.CONFLICT, ex.getMessage(), req, detalles);
    }

    @ExceptionHandler(SerialDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleSerialDuplicado(
            SerialDuplicadoException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            RecursoNoEncontradoException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    // ── LOGÍSTICA ─────────────────────────────────────────────

    @ExceptionHandler(DespachoEstadoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleDespachoEstado(
            DespachoEstadoInvalidoException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    // ── VENTAS ────────────────────────────────────────────────

    @ExceptionHandler(VentaDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleVentaDuplicada(
            VentaDuplicadaException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(VentaYaAnuladaException.class)
    public ResponseEntity<ApiErrorResponse> handleVentaAnulada(
            VentaYaAnuladaException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(PeriodoCerradoException.class)
    public ResponseEntity<ApiErrorResponse> handlePeriodoCerrado(
            PeriodoCerradoException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    // ── VALIDACIÓN (@Valid) ───────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> errores = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.put(fe.getField(), fe.getDefaultMessage());
        }
        return error(HttpStatus.BAD_REQUEST,
                "Error de validación en los datos enviados", req, errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, Object> errores = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                errores.put(cv.getPropertyPath().toString(), cv.getMessage()));
        return error(HttpStatus.BAD_REQUEST, "Parámetros inválidos", req, errores);
    }

    // ── FALLBACK GENÉRICO ─────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {
        // En producción: log.error("Error no manejado", ex)
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Contacta al administrador.", req, null);
    }

    // ── HELPER ───────────────────────────────────────────────

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status,
                                                   String mensaje,
                                                   HttpServletRequest req,
                                                   Map<String, Object> detalles) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(status.name())
                        .mensaje(mensaje)
                        .path(req.getRequestURI())
                        .detalles(detalles)
                        .build());
    }

    // ── DTO de respuesta de error ─────────────────────────────

    @Getter
    @Builder
    public static class ApiErrorResponse {
        private final Instant timestamp;
        private final int status;
        private final String error;
        private final String mensaje;
        private final String path;
        private final Map<String, Object> detalles;
    }
}
