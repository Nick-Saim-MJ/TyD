package com.tyd.kitprepago.modulo_inventario.controller;

import com.tyd.kitprepago.modulo_inventario.dto.response.HistorialCustodioResponse;
import com.tyd.kitprepago.modulo_inventario.dto.response.ItemKitDetalleResponse;
import com.tyd.kitprepago.modulo_inventario.service.HistorialCustodioService;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
class HistorialCustodioController {

    private final HistorialCustodioService historialService;

    /**
     * GET /api/items-kit/{id}/historial
     * Línea de vida completa del kit: todos los cambios de ubicación y custodia.
     * Visible para ADMIN, JEFE_ALMACEN y CONTADOR.
     * Responde con ItemKitDetalleResponse que incluye el kit + su historial.
     */
    @GetMapping("/api/items-kit/{id}/historial")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','CONTADOR')")
    public ResponseEntity<ItemKitDetalleResponse> historial(@PathVariable Long id) {
        return ResponseEntity.ok(historialService.lineaDeVida(id));
    }

    /**
     * GET /api/mis-recepciones
     * Historial personal del almacenero: solo sus propios INGRESOs.
     * Filtra historial_custodios por registrado_por_id = usuarioActual
     * y tipo_evento = INGRESO.
     */
    @GetMapping("/api/mis-recepciones")
    @PreAuthorize("hasAnyRole('ALMACENERO','JEFE_ALMACEN','ADMIN','CONTADOR')")
    public ResponseEntity<List<HistorialCustodioResponse>> misRecepciones(
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(historialService.misRecepciones(usuario));
    }
}
