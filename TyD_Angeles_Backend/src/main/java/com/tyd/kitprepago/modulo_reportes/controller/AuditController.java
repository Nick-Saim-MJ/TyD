package com.tyd.kitprepago.modulo_reportes.controller;

import com.tyd.kitprepago.modulo_reportes.dto.response.AuditLogResponse;
import com.tyd.kitprepago.modulo_reportes.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
class AuditController {

    private final AuditService auditService;

    /**
     * GET /api/audit?tabla=ventas&registroId=42
     * Historial de cambios de un registro específico.
     * Caso de uso del contador: detectó diferencia en monto_venta
     * y el admin investiga quién lo cambió y cuándo.
     */
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> historial(
            @RequestParam String tabla,
            @RequestParam Long registroId) {
        return ResponseEntity.ok(auditService.historialRegistro(tabla, registroId));
    }

    /**
     * GET /api/audit/usuario?usuarioId=X&tabla=ventas&mes=2025-01
     * Actividad completa de un usuario en un periodo y tabla.
     * Para investigar: "¿qué hizo Maricarmen el día del faltante?"
     */
    @GetMapping("/usuario")
    public ResponseEntity<List<AuditLogResponse>> actividadUsuario(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String tabla,
            @RequestParam(required = false) String mes) {
        return ResponseEntity.ok(auditService.actividadUsuario(usuarioId, tabla, mes));
    }
}
