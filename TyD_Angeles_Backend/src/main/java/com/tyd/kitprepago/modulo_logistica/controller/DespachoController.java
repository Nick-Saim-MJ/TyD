package com.tyd.kitprepago.modulo_logistica.controller;

import com.tyd.kitprepago.modulo_inventario.dto.response.ItemKitResponse;
import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import com.tyd.kitprepago.modulo_inventario.mapper.InventarioMapper;
import com.tyd.kitprepago.modulo_inventario.repository.ItemKitRepository;
import com.tyd.kitprepago.modulo_logistica.dto.request.ConfirmarRecepcionRequest;
import com.tyd.kitprepago.modulo_logistica.dto.request.CrearDespachoRequest;
import com.tyd.kitprepago.modulo_logistica.dto.response.ConfirmarRecepcionResponse;
import com.tyd.kitprepago.modulo_logistica.dto.response.DespachoDetalleResponse;
import com.tyd.kitprepago.modulo_logistica.dto.response.DespachoResponse;
import com.tyd.kitprepago.modulo_logistica.service.DespachoService;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
public class DespachoController {

    private final DespachoService despachoService;
    private final ZonaContextHolder zonaCtx;
    private final ItemKitRepository itemKitRepo;
    private final InventarioMapper mapper;

    /**
     * POST /api/despachos
     * ALMACENERO y JEFE_ALMACEN crean el despacho.
     * El servicio valida:
     *  - Todos los kits deben estar DISPONIBLE en la sucursal origen
     *  - ALMACENERO no puede crear despachos inter-zona
     *  - No duplicar kits en despachos activos
     * Al finalizar: kits en estado TRANSITO, despacho EN_TRANSITO.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO','CONTADOR')")
    public ResponseEntity<DespachoResponse> crear(
            @Valid @RequestBody CrearDespachoRequest request,
            @AuthenticationPrincipal UsuarioPrincipal emisor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(despachoService.crear(request, emisor));
    }

    /**
     * GET /api/despachos
     * ALMACENERO ve solo los de su zona (origen O destino).
     * ADMIN y CONTADOR ven todos.
     * ZonaContextHolder aplica el filtro automáticamente.
     */
    @GetMapping
    public ResponseEntity<List<DespachoResponse>> listar() {
        return ResponseEntity.ok(despachoService.listar());
    }

    /**
     * GET /api/despachos/{id}
     * Detalle completo con cada despacho_item y su estado individual.
     * Incluye contadores: totalEnviados, totalRecibidosOk, totalDefectuosos, totalNoRecibidos.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DespachoDetalleResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(despachoService.detalle(id));
    }

    /**
     * GET /api/despachos/pendientes-recepcion
     * Lista los despachos EN_TRANSITO donde sucursal_destino = sucursal del usuario.
     * El almacenero receptor entra aquí para ver qué le llega y confirmar.
     * Ordenados por fecha_despacho ASC (más antiguos primero).
     */
    @GetMapping("/pendientes-recepcion")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO','CONTADOR')")
    public ResponseEntity<List<DespachoResponse>> pendientesRecepcion(
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(despachoService.pendientesRecepcion(usuario));
    }

    /**
     * PUT /api/despachos/{id}/confirmar-recepcion
     *
     * El endpoint más crítico del módulo. El receptor confirma kit por kit.
     * Toda la operación ocurre en una @Transactional en el servicio:
     *
     *  RECIBIDO_OK         → kit DISPONIBLE en destino + historial TRASLADO
     *  RECIBIDO_DEFECTUOSO → kit DEFECTUOSO en destino + historial BAJA + observacion
     *  NO_RECIBIDO         → kit DISPONIBLE en ORIGEN  + historial revertido
     *
     * Si el payload no incluye un kit del despacho → se trata como NO_RECIBIDO.
     * Si cualquier operación falla → rollback completo, ningún kit se mueve.
     *
     * Retorna un resumen: cuántos OK, defectuosos y no recibidos.
     */
    @PutMapping("/{id}/confirmar-recepcion")
    @PreAuthorize("hasAnyRole('ADMIN','JEFE_ALMACEN','ALMACENERO','CONTADOR')")
    public ResponseEntity<ConfirmarRecepcionResponse> confirmarRecepcion(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarRecepcionRequest request,
            @AuthenticationPrincipal UsuarioPrincipal receptor) {
        return ResponseEntity.ok(despachoService.confirmarRecepcion(id, request, receptor));
    }
}
