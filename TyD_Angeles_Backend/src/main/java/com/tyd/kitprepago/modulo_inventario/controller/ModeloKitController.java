package com.tyd.kitprepago.modulo_inventario.controller;

import com.tyd.kitprepago.modulo_inventario.dto.request.CrearModeloKitRequest;
import com.tyd.kitprepago.modulo_inventario.dto.response.ModeloKitResponse;
import com.tyd.kitprepago.modulo_inventario.service.ModeloKitService;
import com.tyd.kitprepago.modulo_inventario.dto.request.*;
import com.tyd.kitprepago.modulo_inventario.dto.response.*;
import com.tyd.kitprepago.modulo_inventario.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modelos-kit")
@RequiredArgsConstructor
class ModeloKitController {

    private final ModeloKitService modeloKitService;

    /**
     * GET /api/modelos-kit
     * Todos los autenticados. Angular lo usa para llenar el selector de modelo
     * en el formulario de ingreso de kits. Si tieneDeco=false, oculta el campo serie_deco.
     */
    @GetMapping
    public ResponseEntity<List<ModeloKitResponse>> listar() {
        return ResponseEntity.ok(modeloKitService.listar());
    }

    /**
     * POST /api/modelos-kit
     * Solo ADMIN. Para registrar nuevos modelos (LH100, LH300, futuros).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModeloKitResponse> crear(
            @Valid @RequestBody CrearModeloKitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modeloKitService.crear(request));
    }

    /**
     * PATCH /api/modelos-kit/{id}/toggle
     * Activa o desactiva un modelo. El frontend deja de mostrarlo en el selector.
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModeloKitResponse> toggleActivo(@PathVariable Long id) {
        return ResponseEntity.ok(modeloKitService.toggleActivo(id));
    }

    /**
     * DELETE /api/modelos-kit/{id}
     * Solo ADMIN. Elimina un modelo si no tiene ítems asociados.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        modeloKitService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
