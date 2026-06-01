package com.tyd.kitprepago.modulo_inventario.service;

import com.tyd.kitprepago.modulo_inventario.dto.request.CrearModeloKitRequest;
import com.tyd.kitprepago.modulo_inventario.dto.response.ModeloKitResponse;
import com.tyd.kitprepago.modulo_inventario.entity.ModeloKit;
import com.tyd.kitprepago.modulo_inventario.mapper.InventarioMapper;
import com.tyd.kitprepago.modulo_inventario.repository.ModeloKitRepository;
import com.tyd.kitprepago.shared.audit.Auditable;
import com.tyd.kitprepago.shared.exception.SerialDuplicadoException;
import com.tyd.kitprepago.modulo_inventario.dto.request.*;
import com.tyd.kitprepago.modulo_inventario.dto.response.*;
import com.tyd.kitprepago.modulo_inventario.entity.*;
import com.tyd.kitprepago.modulo_inventario.repository.*;
import com.tyd.kitprepago.shared.audit.TipoAccion;
import com.tyd.kitprepago.shared.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ModeloKitService {

    private final ModeloKitRepository modeloRepo;
    private final InventarioMapper mapper;
    private final ItemKitRepository itemKitRepo;
    @Transactional(readOnly = true)
    public List<ModeloKitResponse> listar() {
        return mapper.toModeloKitResponseList(modeloRepo.findByActivoTrue());
    }

    @Transactional
    @Auditable(tabla = "modelos_kit", accion = TipoAccion.INSERT)
    public ModeloKitResponse crear(CrearModeloKitRequest req) {
        if (modeloRepo.existsByCodigo(req.codigo())) {
            throw new SerialDuplicadoException("Código de modelo ya existe: " + req.codigo());
        }
        ModeloKit modelo = ModeloKit.builder()
                .codigo(req.codigo().toUpperCase())
                .nombre(req.nombre())
                .descripcion(req.descripcion())
                .tieneDeco(req.tieneDeco() != null ? req.tieneDeco() : true)
                .build();
        return mapper.toModeloKitResponse(modeloRepo.save(modelo));
    }

    @Transactional
    public ModeloKitResponse toggleActivo(Long id) {
        ModeloKit modelo = modeloRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ModeloKit", id));
        modelo.setActivo(!modelo.getActivo());
        return mapper.toModeloKitResponse(modeloRepo.save(modelo));
    }

    @Transactional
    public void eliminar(Long id) {
        ModeloKit modelo = modeloRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("ModeloKit", id));

        // CRUCIAL: Verificar si hay kits que usan este modelo
        // Debes tener este método countByModeloKitId en tu ItemKitRepository
        long cantidadKits = itemKitRepo.countByModeloKitId(id);

        if (cantidadKits > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar el modelo porque tiene " + cantidadKits + " kits asociados. " +
                            "Considere usar el endpoint /toggle para desactivarlo en su lugar.");
        }

        modeloRepo.delete(modelo);
    }
}
