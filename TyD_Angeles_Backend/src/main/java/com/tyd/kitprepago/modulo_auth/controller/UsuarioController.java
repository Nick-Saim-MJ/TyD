package com.tyd.kitprepago.modulo_auth.controller;

import com.tyd.kitprepago.modulo_auth.dto.request.*;
import com.tyd.kitprepago.modulo_auth.dto.response.*;
import com.tyd.kitprepago.modulo_auth.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /api/usuarios
     * Solo ADMIN. Retorna todos los usuarios, filtrados por zona si hay query param.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    /**
     * GET /api/usuarios/vendedores?zonaId=1
     * Cualquier autenticado. Para el autocomplete en el formulario de venta.
     * Retorna VENDEDOR + ALMACENERO + JEFE_ALMACEN de la zona indicada.
     */
    @GetMapping("/vendedores")
    public ResponseEntity<List<UsuarioResponse>> listarVendedores(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) Long sucursalId) {
        return ResponseEntity.ok(usuarioService.listarVendedores(zonaId, sucursalId));
    }

    /**
     * POST /api/usuarios
     * Solo ADMIN. Crea un usuario y le asigna zona/sucursal.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody CrearUsuarioRequest request,
            @AuthenticationPrincipal UsuarioPrincipal admin) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.crear(request, admin));
    }

    /**
     * PUT /api/usuarios/{id}
     * Solo ADMIN. Un usuario no puede cambiar su propio rol.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<UsuarioResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarUsuarioRequest request,
            @AuthenticationPrincipal UsuarioPrincipal admin) {
        return ResponseEntity.ok(usuarioService.editar(id, request, admin));
    }

    /**
     * DELETE /api/usuarios/{id}
     * Solo ADMIN. Soft delete — solo actualiza deleted_at.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTADOR')")
    public ResponseEntity<UsuarioResponse> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioPrincipal admin) {
        return ResponseEntity.ok(usuarioService.softDelete(id, admin));
    }
}
