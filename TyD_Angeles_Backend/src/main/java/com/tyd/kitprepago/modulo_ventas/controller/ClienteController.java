package com.tyd.kitprepago.modulo_ventas.controller;

import com.tyd.kitprepago.modulo_ventas.dto.request.CrearClienteRequest;
import com.tyd.kitprepago.modulo_ventas.dto.response.ClienteResponse;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import com.tyd.kitprepago.modulo_ventas.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
class ClienteController {

    private final ClienteService clienteService;

    /**
     * GET /api/clientes/buscar?q=74129067
     * Autocomplete para el formulario de venta.
     * Busca por DNI, nombre, apellido o RUC con límite de 10 resultados.
     * Requiere mínimo 2 caracteres para evitar queries muy amplias.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(clienteService.buscar(q));
    }

    /**
     * GET /api/clientes
     * Solo ADMIN y CONTADOR. Lista completa con filtro por tipo.
     * ?tipo=PDV | ?tipo=GENERAL | sin filtro = todos
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<List<ClienteResponse>> listar(
            @RequestParam(required = false) TipoCliente tipo) {
        return ResponseEntity.ok(clienteService.listar(tipo));
    }

    /**
     * POST /api/clientes
     * Cualquier autenticado puede crear un cliente.
     * El vendedor lo usa inline desde el formulario de venta cuando el cliente es nuevo.
     * El servicio verifica que DNI y RUC no estén duplicados.
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(
            @Valid @RequestBody CrearClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.crear(request));
    }
}
