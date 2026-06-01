package com.tyd.kitprepago.modulo_auth.service;

import com.tyd.kitprepago.modulo_auth.dto.request.CrearZonaRequest;
import com.tyd.kitprepago.modulo_auth.dto.request.EditarZonaRequest;
import com.tyd.kitprepago.modulo_auth.dto.response.ZonaResponse;
import com.tyd.kitprepago.modulo_auth.entity.Zona;
import com.tyd.kitprepago.modulo_auth.mapper.AuthMapper;
import com.tyd.kitprepago.shared.audit.Auditable;
import com.tyd.kitprepago.shared.audit.TipoAccion;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZonaService {

    private final JpaRepository<Zona, Long> zonaRepo;
    private final AuthMapper mapper;

    @Transactional(readOnly = true)
    public List<ZonaResponse> listar() {
        return mapper.toZonaResponseList(zonaRepo.findAll());
    }

    @Transactional(readOnly = true)
    public ZonaResponse buscarPorId(Long id) {
        return zonaRepo.findById(id)
                .map(mapper::toZonaResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona", id));
    }

    @Transactional
    @Auditable(tabla = "zonas", accion = TipoAccion.INSERT)
    public ZonaResponse crear(CrearZonaRequest req) {
        Zona zona = Zona.builder()
                .codigoZona(req.codigoZona())
                .nombre(req.nombre())
                .region(req.region())
                .build();
        return mapper.toZonaResponse(zonaRepo.save(zona));
    }

    @Transactional
    @Auditable(tabla = "zonas", accion = TipoAccion.UPDATE)
    public ZonaResponse editar(Long id, EditarZonaRequest req) {
        Zona zona = zonaRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona", id));
        Optional.ofNullable(req.codigoZona()).ifPresent(zona::setCodigoZona);
        Optional.ofNullable(req.nombre()).ifPresent(zona::setNombre);
        Optional.ofNullable(req.region()).ifPresent(zona::setRegion);
        Optional.ofNullable(req.activo()).ifPresent(zona::setActivo);
        return mapper.toZonaResponse(zonaRepo.save(zona));
    }
}
