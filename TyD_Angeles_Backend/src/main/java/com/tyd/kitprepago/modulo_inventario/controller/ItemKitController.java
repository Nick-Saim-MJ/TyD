package com.tyd.kitprepago.modulo_inventario.controller;

import com.tyd.kitprepago.modulo_inventario.dto.request.CambiarEstadoKitRequest;
import com.tyd.kitprepago.modulo_inventario.dto.response.ItemKitResponse;
import com.tyd.kitprepago.modulo_inventario.service.ItemKitService;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items-kit")
@RequiredArgsConstructor
class ItemKitController {

    private final ItemKitService itemKitService;

    /**
     * GET /api/items-kit/buscar?serie=K10RBA50E0093
     * Búsqueda por cualquiera de los tres seriales.
     * Caso de uso principal: el almacenero escanea un código de barras.
     * Toca los tres índices definidos en la tabla.
     */
    @GetMapping("/buscar")
    public ResponseEntity<ItemKitResponse> buscarPorSerial(@RequestParam String serie) {
        return ResponseEntity.ok(itemKitService.buscarPorSerial(serie));
    }

    /**
     * GET /api/items-kit/por-boucher?numeroOperacion=7224587
     * El filtro especial del almacenero. Devuelve todos los kits
     * del lote con ese N° de boucher/operación y pinta la info completa.
     * ZonaContextHolder valida que el almacenero tenga acceso a esa zona.
     */
    @GetMapping("/por-boucher")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO','CONTADOR')")
    public ResponseEntity<List<ItemKitResponse>> porBoucher(
            @RequestParam String numeroOperacion) {
        return ResponseEntity.ok(itemKitService.buscarPorBoucher(numeroOperacion));
    }

    /**
     * GET /api/items-kit/zona/{zonaId}
     * Todos los kits DISPONIBLES de una zona (almacén + todas sus oficinas).
     * Lo usan vendedores y almaceneros para ver el inventario de su región.
     * Query: JOIN sucursales WHERE zona_id = :zonaId AND estado = DISPONIBLE
     */
    @GetMapping("/zona/{zonaId}")
    public ResponseEntity<List<ItemKitResponse>> porZona(@PathVariable Long zonaId) {
        return ResponseEntity.ok(itemKitService.listarDisponiblesPorZona(zonaId));
    }

    /**
     * PATCH /api/items-kit/{id}/estado
     * Solo ADMIN y JEFE_ALMACEN. Permite marcar un kit como DEFECTUOSO o DEVUELTO.
     * VENDIDO y TRANSITO no están disponibles aquí — los manejan venta/despacho.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','CONTADOR')")
    public ResponseEntity<ItemKitResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoKitRequest request,
            @AuthenticationPrincipal UsuarioPrincipal editor) {
        return ResponseEntity.ok(itemKitService.cambiarEstado(id, request, editor));
    }
}
