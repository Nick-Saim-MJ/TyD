package com.tyd.kitprepago.modulo_auth.controller;

import com.tyd.kitprepago.modulo_auth.dto.request.*;
import com.tyd.kitprepago.modulo_auth.dto.response.*;
import com.tyd.kitprepago.modulo_auth.service.SucursalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
class SucursalController {

    private final SucursalService sucursalService;

    /**
     * GET /api/sucursales
     * Todos los autenticados. ZonaContextHolder filtra automáticamente:
     *  - ADMIN/CONTADOR → todas las sucursales
     *  - Resto           → solo las de su zona
     */
    @GetMapping
    public ResponseEntity<List<SucursalResponse>> listar() {
        return ResponseEntity.ok(sucursalService.listar());
    }

    /**
     * GET /api/sucursales/{id}/stock
     * Todos los autenticados (con validación de zona interna).
     * Retorna el COUNT de items_kit con estado=DISPONIBLE en tiempo real.
     * NO usa kardex_mensual — el kardex es para reportes históricos cerrados.
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockSucursalResponse> stock(@PathVariable Long id) {
        return ResponseEntity.ok(sucursalService.obtenerStock(id));
    }


    /**
     * POST /api/sucursales
     * Solo ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<SucursalResponse> crear(
            @Valid @RequestBody CrearSucursalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sucursalService.crear(request));
    }
}