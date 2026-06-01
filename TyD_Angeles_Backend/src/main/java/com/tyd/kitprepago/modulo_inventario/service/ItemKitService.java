package com.tyd.kitprepago.modulo_inventario.service;

import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_inventario.dto.request.CambiarEstadoKitRequest;
import com.tyd.kitprepago.modulo_inventario.dto.response.ItemKitResponse;
import com.tyd.kitprepago.modulo_inventario.entity.EstadoKit;
import com.tyd.kitprepago.modulo_inventario.entity.HistorialCustodio;
import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import com.tyd.kitprepago.modulo_inventario.entity.TipoEvento;
import com.tyd.kitprepago.modulo_inventario.mapper.InventarioMapper;
import com.tyd.kitprepago.modulo_inventario.repository.HistorialCustodioRepository;
import com.tyd.kitprepago.modulo_inventario.repository.ItemKitRepository;
import com.tyd.kitprepago.modulo_inventario.repository.LoteRepository;
import com.tyd.kitprepago.shared.audit.Auditable;
import com.tyd.kitprepago.shared.audit.TipoAccion;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemKitService {

    private final ItemKitRepository itemKitRepo;
    private final LoteRepository loteRepo;
    private final HistorialCustodioRepository historialRepo;
    private final InventarioMapper mapper;
    private final ZonaContextHolder zonaCtx;
    private final JpaRepository<Usuario, Long> usuarioRepo;

    @Transactional(readOnly = true)
    public ItemKitResponse buscarPorSerial(String serie) {
        ItemKit kit = itemKitRepo.findBySerialCualquiera(serie)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Kit con serial '" + serie + "' no encontrado"));

        // Validar que el usuario tiene acceso a la zona del kit
        if (kit.getSucursalActual() != null) {
            zonaCtx.validarAccesoZona(kit.getSucursalActual().getZona().getId());
        }
        return mapper.toItemKitResponse(kit);
    }

    @Transactional(readOnly = true)
    public List<ItemKitResponse> buscarPorBoucher(String numeroOperacion) {
        List<ItemKit> kits = itemKitRepo.findByBoucher(numeroOperacion);
        if (kits.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "No se encontraron kits con N° Operación: " + numeroOperacion);
        }
        // Validar zona con el primer kit (todos pertenecen al mismo lote/zona)
        /*kits.get(0).getLote().getZona();
        zonaCtx.validarAccesoZona(kits.get(0).getLote().getZona().getId());
        return mapper.toItemKitResponseList(kits);*/
        Long zonaId = kits.get(0).getLote().getZona().getId();

        zonaCtx.validarAccesoZona(zonaId);

        return mapper.toItemKitResponseList(kits);
    }

    @Transactional(readOnly = true)
    public List<ItemKitResponse> listarDisponiblesPorZona(Long zonaId) {
        zonaCtx.validarAccesoZona(zonaId);
        return mapper.toItemKitResponseList(itemKitRepo.findDisponiblesPorZona(zonaId));
    }

    @Transactional
    @Auditable(tabla = "items_kit", accion = TipoAccion.UPDATE)
    public ItemKitResponse cambiarEstado(Long id, CambiarEstadoKitRequest req,
                                         UsuarioPrincipal editor) {
        // Solo DEFECTUOSO o DEVUELTO desde este endpoint
        if (req.nuevoEstado() == EstadoKit.VENDIDO || req.nuevoEstado() == EstadoKit.TRANSITO) {
            throw new IllegalArgumentException(
                    "Los estados VENDIDO y TRANSITO se manejan internamente por los flujos de venta/despacho");
        }

        ItemKit kit = itemKitRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ItemKit", id));

        if (kit.getSucursalActual() != null) {
            zonaCtx.validarAccesoZona(kit.getSucursalActual().getZona().getId());
        }

        EstadoKit estadoAnterior = kit.getEstado();
        kit.setEstado(req.nuevoEstado());
        itemKitRepo.save(kit);

        Usuario registrador = usuarioRepo.findById(editor.getId()).orElseThrow();

        // Registrar en historial
        historialRepo.save(HistorialCustodio.builder()
                .itemKit(kit)
                .sucursalAnterior(kit.getSucursalActual())
                .sucursalNueva(kit.getSucursalActual())
                .custodioAnterior(kit.getCustodioActual())
                .custodioNuevo(registrador)
                .tipoEvento(TipoEvento.BAJA)
                .motivo(req.motivo())
                .referenciaTipo("AJUSTE_MANUAL")
                .registradoPor(registrador)
                .build());

        log.info("Kit {} cambió estado: {} → {} | motivo: {}",
                kit.getSerieMaestro(), estadoAnterior, req.nuevoEstado(), req.motivo());

        return mapper.toItemKitResponse(kit);
    }
}
