package com.tyd.kitprepago.modulo_ventas.controller;

import com.tyd.kitprepago.modulo_ventas.dto.request.AprobarLiquidacionRequest;
import com.tyd.kitprepago.modulo_ventas.dto.request.CrearLiquidacionRequest;
import com.tyd.kitprepago.modulo_ventas.dto.response.LiquidacionResponse;
import com.tyd.kitprepago.modulo_ventas.entity.EstadoLiquidacion;
import com.tyd.kitprepago.modulo_ventas.service.LiquidacionService;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/liquidaciones")
@RequiredArgsConstructor
class LiquidacionController {

    private final LiquidacionService liquidacionService;

    /**
     * GET /api/liquidaciones
     * - VENDEDOR/ALMACENERO: ve solo las suyas
     * - ADMIN/JEFE/CONTADOR: ve todas con filtros opcionales
     * ?vendedorId=X &estado=PENDIENTE
     */
    @GetMapping
    public ResponseEntity<List<LiquidacionResponse>> listar(
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) EstadoLiquidacion estado,
            @AuthenticationPrincipal UsuarioPrincipal solicitante) {
        return ResponseEntity.ok(liquidacionService.listar(vendedorId, estado, solicitante));
    }

    /**
     * POST /api/liquidaciones
     * ALMACENERO o VENDEDOR crea la liquidación del periodo.
     * El sistema calcula monto_total_esperado = SUM(ventas no liquidadas del periodo).
     * La columna `diferencia` la calcula MySQL automáticamente al aprobar.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO','VENDEDOR')")
    public ResponseEntity<LiquidacionResponse> crear(
            @Valid @RequestBody CrearLiquidacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(liquidacionService.crear(request));
    }

    /**
     * PUT /api/liquidaciones/{id}/aprobar
     * Solo ADMIN. Registra monto_depositado y cambia el estado.
     * Al aprobar: vincula las ventas del periodo a esta liquidación.
     * La columna `diferencia` (= depositado - esperado) la calcula MySQL sola.
     */
    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LiquidacionResponse> aprobar(
            @PathVariable Long id,
            @Valid @RequestBody AprobarLiquidacionRequest request,
            @AuthenticationPrincipal UsuarioPrincipal admin) {
        return ResponseEntity.ok(liquidacionService.aprobar(id, request, admin));
    }
}
