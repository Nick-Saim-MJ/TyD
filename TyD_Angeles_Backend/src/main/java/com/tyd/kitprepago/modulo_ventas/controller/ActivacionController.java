package com.tyd.kitprepago.modulo_ventas.controller;

import com.tyd.kitprepago.modulo_ventas.dto.request.AnularVentaRequest;
import com.tyd.kitprepago.modulo_ventas.dto.request.ConfirmarActivacionRequest;
import com.tyd.kitprepago.modulo_ventas.dto.request.RegistrarVentaRequest;
import com.tyd.kitprepago.modulo_ventas.dto.response.ActivacionResponse;
import com.tyd.kitprepago.modulo_ventas.dto.response.RegistrarVentaResponse;
import com.tyd.kitprepago.modulo_ventas.dto.response.VentaResponse;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import com.tyd.kitprepago.modulo_ventas.service.ActivacionService;
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
@RequestMapping("/api/activaciones")
@RequiredArgsConstructor
class ActivacionController {

    private final ActivacionService activacionService;

    /**
     * GET /api/activaciones/pendientes
     * Lista activaciones PENDIENTE filtradas por zona del usuario.
     * El almacenero o jefe confirma cuando DirecTV procesa la señal.
     * Ordenadas por fecha_venta ASC (más antiguas primero).
     */
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO')")
    public ResponseEntity<List<ActivacionResponse>> pendientes() {
        return ResponseEntity.ok(activacionService.listarPendientes());
    }

    /**
     * PUT /api/activaciones/{id}/confirmar
     * Marca la activación como ACTIVO con fecha_activacion = NOW().
     * Puede registrar monto_recarga_inicial si no se hizo al vender.
     */
    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO')")
    public ResponseEntity<ActivacionResponse> confirmar(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarActivacionRequest request,
            @AuthenticationPrincipal UsuarioPrincipal confirmador) {
        return ResponseEntity.ok(activacionService.confirmar(id, request, confirmador));
    }
}
