package com.tyd.kitprepago.modulo_reportes.controller;

import com.tyd.kitprepago.modulo_reportes.dto.response.GenerarKardexResponse;
import com.tyd.kitprepago.modulo_reportes.dto.response.KardexResponse;
import com.tyd.kitprepago.modulo_reportes.service.KardexService;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kardex")
@RequiredArgsConstructor
class KardexController {

    private final KardexService kardexService;

    /**
     * GET /api/kardex?sucursalId=X&periodo=2025-01
     * CONTADOR puede consultar pero no modificar.
     * Devuelve solo kardex cerrados al CONTADOR para garantizar datos auditables.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','CONTADOR')")
    public ResponseEntity<List<KardexResponse>> consultar(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) String periodo) {
        return ResponseEntity.ok(kardexService.consultar(sucursalId, periodo));
    }

    /**
     * POST /api/kardex/generar?sucursalId=X&periodo=2025-03
     * Solo ADMIN y JEFE_ALMACEN.
     * Calcula el kardex del periodo desde historial_custodios y lo persiste.
     * Si ya existía un kardex abierto para esa sucursal/periodo → lo recalcula.
     * Si está cerrado → 409 PeriodoCerradoException.
     */
    @PostMapping("/generar")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','CONTADOR')")
    public ResponseEntity<GenerarKardexResponse> generar(
            @RequestParam Long sucursalId,
            @RequestParam String periodo,
            @AuthenticationPrincipal UsuarioPrincipal generador) {
        return ResponseEntity.ok(kardexService.generar(sucursalId, periodo, generador));
    }

    /**
     * POST /api/kardex/{id}/cerrar
     * Solo ADMIN y JEFE_ALMACEN.
     * Bloquea el periodo: cerrado = TRUE.
     * El contador solo puede auditar kardex cerrados — datos congelados e inmutables.
     */
    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','CONTADOR')")
    public ResponseEntity<KardexResponse> cerrar(@PathVariable Long id) {
        return ResponseEntity.ok(kardexService.cerrar(id));
    }

    /**
     * POST /api/kardex/{id}/reabrir
     * Solo ADMIN. Para corregir errores en un kardex ya cerrado.
     * Se registra en audit_log automáticamente vía @Auditable.
     */
    @PostMapping("/{id}/reabrir")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<KardexResponse> reabrir(@PathVariable Long id) {
        return ResponseEntity.ok(kardexService.reabrir(id));
    }
}
