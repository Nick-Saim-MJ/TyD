package com.tyd.kitprepago.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Estructura uniforme de respuesta de error para todos los endpoints.
 * Angular siempre recibirá este formato cuando el status sea 4xx o 5xx.
 *
 * Ejemplo de respuesta 409 CONFLICT al intentar vender un kit no disponible:
 * {
 *   "timestamp": "2025-03-15T14:22:00Z",
 *   "status": 409,
 *   "error": "CONFLICT",
 *   "mensaje": "El kit 'K10RBA50E0093' no está disponible.",
 *   "path": "/api/ventas",
 *   "erroresValidacion": null
 * }
 *
 * Ejemplo 400 con errores de @Valid:
 * {
 *   "timestamp": "...",
 *   "status": 400,
 *   "error": "BAD_REQUEST",
 *   "mensaje": "Error de validación en los datos enviados.",
 *   "path": "/api/lotes",
 *   "erroresValidacion": [
 *     { "campo": "numeroOperacion", "mensaje": "No puede estar vacío" },
 *     { "campo": "cantidadEsperada", "mensaje": "Debe ser mayor que 0" }
 *   ]
 * }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String mensaje;
    private final String path;

    /** Solo presente en errores de validación (@Valid) */
    private final List<CampoError> erroresValidacion;

    @Getter
    @Builder
    public static class CampoError {
        private final String campo;
        private final String mensaje;
    }
}
