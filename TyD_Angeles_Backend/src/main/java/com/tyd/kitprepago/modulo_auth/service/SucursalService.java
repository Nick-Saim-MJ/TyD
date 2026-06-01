package com.tyd.kitprepago.modulo_auth.service;

import com.tyd.kitprepago.modulo_auth.dto.request.CrearSucursalRequest;
import com.tyd.kitprepago.modulo_auth.dto.response.StockSucursalResponse;
import com.tyd.kitprepago.modulo_auth.dto.response.SucursalResponse;
import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Zona;
import com.tyd.kitprepago.modulo_auth.mapper.AuthMapper;
import com.tyd.kitprepago.modulo_auth.repository.SucursalJpaRepository;
import com.tyd.kitprepago.shared.audit.Auditable;
import com.tyd.kitprepago.shared.audit.TipoAccion;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
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
public class SucursalService {

    private final SucursalJpaRepository sucursalRepo;
    private final JpaRepository<Zona, Long> zonaRepo;
    private final AuthMapper mapper;
    private final ZonaContextHolder zonaCtx;

    @Transactional(readOnly = true)
    public List<SucursalResponse> listar() {
        List<Sucursal> sucursales = zonaCtx.getZonaIdFiltro()
                .map(sucursalRepo::findByZonaIdAndDeletedAtIsNull)
                .orElseGet(sucursalRepo::findByDeletedAtIsNull);
        return mapper.toSucursalResponseList(sucursales);
    }

    @Transactional(readOnly = true)
    public StockSucursalResponse obtenerStock(Long sucursalId) {
        Sucursal sucursal = sucursalRepo.findById(sucursalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", sucursalId));

        // El filtro de zona aplica: un VENDEDOR no puede ver el stock de otra zona
        zonaCtx.validarAccesoZona(sucursal.getZona().getId());

        Long disponibles = sucursalRepo.contarStockDisponible(sucursalId);
        return new StockSucursalResponse(sucursalId, sucursal.getNombre(), disponibles);
    }

    @Transactional
    @Auditable(tabla = "sucursales", accion = TipoAccion.INSERT)
    public SucursalResponse crear(CrearSucursalRequest req) {
        Zona zona = zonaRepo.findById(req.zonaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona", req.zonaId()));

        Sucursal ubicacionFisica = null;
        if (req.ubicacionFisicaId() != null) {
            ubicacionFisica = sucursalRepo.findById(req.ubicacionFisicaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal",
                            req.ubicacionFisicaId()));
        }

        Sucursal nueva = Sucursal.builder()
                .nombre(req.nombre())
                .tipo(req.tipo())
                .zona(zona)
                .ubigeo(req.ubigeo())
                .direccion(req.direccion())
                .ubicacionFisica(ubicacionFisica)
                .build();

        return mapper.toSucursalResponse(sucursalRepo.save(nueva));
    }
}
