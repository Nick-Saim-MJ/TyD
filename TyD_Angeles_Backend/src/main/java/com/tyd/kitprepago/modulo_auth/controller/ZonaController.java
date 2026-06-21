package com.tyd.kitprepago.modulo_auth.controller;

import com.tyd.kitprepago.modulo_auth.dto.request.*;
import com.tyd.kitprepago.modulo_auth.dto.response.*;
import com.tyd.kitprepago.modulo_auth.service.ZonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
class ZonaController {

    private final ZonaService zonaService;

    /**
     * GET /api/zonas
     * Todos los autenticados. Las zonas son datos maestros visibles para todos.
     * Angular las usa para el selector de zona y en los formularios de creación.
     */
    @GetMapping
    public ResponseEntity<List<ZonaResponse>> listar() {
        return ResponseEntity.ok(zonaService.listar());
    }

    /**
     * GET /api/zonas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ZonaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(zonaService.buscarPorId(id));
    }

    /**
     * POST /api/zonas
     * Solo ADMIN. Se usa raramente — solo al agregar una nueva región DirecTV.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<ZonaResponse> crear(
            @Valid @RequestBody CrearZonaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(zonaService.crear(request));
    }

    /**
     * PUT /api/zonas/{id}
     * Solo ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<ZonaResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarZonaRequest request) {
        return ResponseEntity.ok(zonaService.editar(id, request));
    }
}
