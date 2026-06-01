package com.tyd.kitprepago.modulo_inventario.controller;

import com.tyd.kitprepago.modulo_inventario.dto.request.CrearLoteRequest;
import com.tyd.kitprepago.modulo_inventario.dto.response.ItemKitResponse;
import com.tyd.kitprepago.modulo_inventario.dto.response.LoteResponse;
import com.tyd.kitprepago.modulo_inventario.service.LoteService;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
class LoteController {

    private final LoteService loteService;

    /**
     * POST /api/lotes
     * Solo ADMIN. Registra el pedido a DirecTV.
     * El payload puede incluir los kits del lote o registrarlos después.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoteResponse> crear(
            @Valid @RequestBody CrearLoteRequest request,
            @AuthenticationPrincipal UsuarioPrincipal admin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loteService.crear(request, admin));
    }

    /**
     * GET /api/lotes
     * ADMIN y CONTADOR ven todo. ALMACENERO solo ve su zona.
     * ZonaContextHolder aplica el filtro automáticamente en LoteService.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO','CONTADOR')")
    public ResponseEntity<List<LoteResponse>> listar(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) String periodo) {

        return ResponseEntity.ok(loteService.listar(zonaId, periodo));
    }

    /**
     * GET /api/lotes/export
     * Descarga Excel con filtros opcionales.
     * El header Content-Disposition le indica al browser que descargue el archivo.
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','CONTADOR')")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) String periodo) {

        byte[] excel = loteService.exportarExcel(zonaId, periodo);

        String filename = "lotes" + (periodo != null ? "-" + periodo : "-" + LocalDate.now()) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(excel);
    }

    /**
     * GET /api/lotes/{id}/items
     * Lista todos los kits de un lote con su estado actual.
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemKitResponse>> items(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.listarItemsDeLote(id));
    }
}
