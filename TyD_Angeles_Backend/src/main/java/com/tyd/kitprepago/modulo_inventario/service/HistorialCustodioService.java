package com.tyd.kitprepago.modulo_inventario.service;

import com.tyd.kitprepago.modulo_inventario.dto.response.HistorialCustodioResponse;
import com.tyd.kitprepago.modulo_inventario.dto.response.ItemKitDetalleResponse;
import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import com.tyd.kitprepago.modulo_inventario.mapper.InventarioMapper;
import com.tyd.kitprepago.modulo_inventario.repository.HistorialCustodioRepository;
import com.tyd.kitprepago.modulo_inventario.repository.ItemKitRepository;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistorialCustodioService {

    private final HistorialCustodioRepository historialRepo;
    private final ItemKitRepository itemKitRepo;
    private final InventarioMapper mapper;
    private final ZonaContextHolder zonaCtx;

    @Transactional(readOnly = true)
    public ItemKitDetalleResponse lineaDeVida(Long itemKitId) {
        ItemKit kit = itemKitRepo.findById(itemKitId)
                .orElseThrow(() -> new RecursoNoEncontradoException("ItemKit", itemKitId));

        if (kit.getSucursalActual() != null) {
            zonaCtx.validarAccesoZona(kit.getSucursalActual().getZona().getId());
        }

        List<HistorialCustodioResponse> historial =
                mapper.toHistorialResponseList(historialRepo.findLineaDeVida(itemKitId));

        return new ItemKitDetalleResponse(
                kit.getId(),
                kit.getSerieMaestro(), kit.getSerieSim(), kit.getSerieDeco(),
                kit.getEstado(),
                kit.getSucursalActual() != null ? kit.getSucursalActual().getNombre() : null,
                kit.getSucursalActual() != null ? kit.getSucursalActual().getZona().getNombre() : null,
                kit.getProducto() != null ? kit.getProducto().getNombre() : null,
                kit.getModeloKit() != null ? kit.getModeloKit().getCodigo() : null,
                kit.getFechaIngreso(),
                historial
        );
    }

    @Transactional(readOnly = true)
    public List<HistorialCustodioResponse> misRecepciones(UsuarioPrincipal usuario) {
        return mapper.toHistorialResponseList(
                historialRepo.findRecepcionesPorUsuario(usuario.getId()));
    }
}
