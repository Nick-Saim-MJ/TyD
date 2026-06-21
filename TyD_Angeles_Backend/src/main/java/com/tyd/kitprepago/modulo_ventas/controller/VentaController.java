package com.tyd.kitprepago.modulo_ventas.controller;

import com.tyd.kitprepago.modulo_ventas.dto.request.AnularVentaRequest;
import com.tyd.kitprepago.modulo_ventas.dto.request.RegistrarVentaRequest;
import com.tyd.kitprepago.modulo_ventas.dto.response.RegistrarVentaResponse;
import com.tyd.kitprepago.modulo_ventas.dto.response.VentaResponse;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import com.tyd.kitprepago.modulo_ventas.service.VentaService;
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
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
class VentaController {

    private final VentaService ventaService;

    /**
     * POST /api/ventas
     * El @Transactional más crítico del sistema.
     * Accesible para VENDEDOR y ALMACENERO (y superiores).
     * <p>
     * Secuencia atómica en el servicio:
     * 1. SELECT FOR UPDATE en items_kit (previene race condition)
     * 2. Verificar estado = DISPONIBLE
     * 3. Resolver o crear el cliente
     * 4. INSERT venta (estado = ACTIVA)
     * 5. INSERT activación (estado = PENDIENTE)
     * 6. UPDATE kit → VENDIDO
     * 7. INSERT historial_custodios (tipo = VENTA)
     * <p>
     * Si el kit ya tiene una venta ACTIVA:
     * MySQL lanza ConstraintViolationException sobre el UNIQUE item_kit_activo
     * → GlobalExceptionHandler lo convierte en 409 CONFLICT.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO','VENDEDOR','CONTADOR')")
    public ResponseEntity<RegistrarVentaResponse> registrar(
            @Valid @RequestBody RegistrarVentaRequest request,
            @AuthenticationPrincipal UsuarioPrincipal creador) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ventaService.registrar(request, creador));
    }

    /**
     * GET /api/ventas
     * Historial filtrado automáticamente por ZonaContextHolder:
     * - VENDEDOR      → solo su sucursal
     * - ALMACENERO    → toda su zona
     * - JEFE_ALMACEN  → toda su zona
     * - ADMIN/CONTADOR → todo
     * <p>
     * Parámetros opcionales (ADMIN/CONTADOR pueden usar todos):
     * ?sucursalId=X &mes=2025-01 &tipo=PDV &vendedorId=X
     */
    @GetMapping
    public ResponseEntity<List<VentaResponse>> listar(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) TipoCliente tipo,
            @RequestParam(required = false) String mes) {
        return ResponseEntity.ok(ventaService.listar(sucursalId, vendedorId, tipo, mes));
    }

    /**
     * POST /api/ventas/{id}/anular
     * Solo ADMIN y JEFE_ALMACEN.
     * Libera el kit (DISPONIBLE), cancela la activación si estaba PENDIENTE.
     * El UNIQUE item_kit_activo queda libre para una nueva venta del mismo kit.
     */
    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','CONTADOR')")
    public ResponseEntity<VentaResponse> anular(
            @PathVariable Long id,
            @Valid @RequestBody AnularVentaRequest request,
            @AuthenticationPrincipal UsuarioPrincipal editor) {
        return ResponseEntity.ok(ventaService.anular(id, request, editor));
    }
}
