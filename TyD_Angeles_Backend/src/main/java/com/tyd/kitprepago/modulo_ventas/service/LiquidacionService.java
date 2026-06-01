package com.tyd.kitprepago.modulo_ventas.service;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_ventas.dto.request.*;
import com.tyd.kitprepago.modulo_ventas.dto.response.*;
import com.tyd.kitprepago.modulo_ventas.entity.*;
import com.tyd.kitprepago.modulo_ventas.mapper.VentasMapper;
import com.tyd.kitprepago.modulo_ventas.repository.*;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiquidacionService {

    private final LiquidacionCajaRepository liquidacionRepo;
    private final VentaRepository ventaRepo;
    private final VentasMapper mapper;
    private final ZonaContextHolder zonaCtx;
    private final JpaRepository<Usuario, Long> usuarioRepo;
    private final JpaRepository<Sucursal, Long> sucursalRepo;

    @Transactional(readOnly = true)
    public List<LiquidacionResponse> listar(Long vendedorIdParam, EstadoLiquidacion estado,
                                             UsuarioPrincipal solicitante) {
        return switch (solicitante.getRol()) {
            // VENDEDOR y ALMACENERO solo ven las suyas
            case VENDEDOR, ALMACENERO -> mapper.toLiquidacionResponseList(
                    liquidacionRepo.findByVendedorIdOrderByFechaLiquidacionDesc(solicitante.getId()));

            // ADMIN y CONTADOR ven todo con filtros
            case ADMIN, CONTADOR, JEFE_ALMACEN -> {
                Long zonaId = zonaCtx.getZonaIdFiltro().orElse(null);
                yield mapper.toLiquidacionResponseList(
                        liquidacionRepo.findConFiltros(zonaId, vendedorIdParam, estado));
            }
        };
    }

    /**
     * Crea la liquidación del periodo.
     * Calcula automáticamente monto_total_esperado sumando las ventas
     * ACTIVAS no liquidadas del vendedor en el periodo indicado.
     */
    @Transactional
    public LiquidacionResponse crear(CrearLiquidacionRequest req) {
        // Validar que no exista ya una liquidación activa para ese vendedor/periodo
        if (liquidacionRepo.existeLiquidacionActivaPorPeriodo(
                req.vendedorId(), req.periodoInicio(), req.periodoFin())) {
            throw new IllegalStateException(
                    "Ya existe una liquidación activa para el vendedor " + req.vendedorId()
                    + " en el periodo " + req.periodoInicio() + " / " + req.periodoFin());
        }

        Instant desde = req.periodoInicio().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant hasta = req.periodoFin().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        // Calcular monto esperado sumando ventas no liquidadas del periodo
        var montoEsperado = ventaRepo.sumMontoNoLiquidado(req.vendedorId(), desde, hasta);

        Usuario vendedor  = usuarioRepo.findById(req.vendedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario vendedor", req.vendedorId()));
        Sucursal sucursal = sucursalRepo.findById(req.sucursalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", req.sucursalId()));

        if (!vendedor.getSucursal().getId().equals(sucursal.getId())) {
            throw new IllegalArgumentException(String.format(
                    "Inconsistencia de sucursal: El vendedor %s pertenece a la sucursal '%s', " +
                            "pero se intentó liquidar en la sucursal '%s'.",
                    vendedor.getNombreCompleto(),
                    vendedor.getSucursal().getNombre(),
                    sucursal.getNombre()
            ));
        }

        LiquidacionCaja liquidacion = LiquidacionCaja.builder()
                .vendedor(vendedor)
                .sucursal(sucursal)
                .periodoInicio(req.periodoInicio())
                .periodoFin(req.periodoFin())
                .montoTotalEsperado(montoEsperado)
                .observaciones(req.observaciones())
                .fechaLiquidacion(Instant.now())
                .build();

        liquidacion = liquidacionRepo.save(liquidacion);

        log.info("Liquidación {} creada: vendedor={} periodo={}/{} monto={}",
                liquidacion.getId(), req.vendedorId(),
                req.periodoInicio(), req.periodoFin(), montoEsperado);

        return mapper.toLiquidacionResponse(liquidacion);
    }

    /**
     * ADMIN aprueba, rechaza u observa la liquidación.
     * Al aprobar: registra monto_depositado, fecha_aprobacion y vincula
     * las ventas del periodo a esta liquidación (UPDATE en batch).
     */
    @Transactional
    public LiquidacionResponse aprobar(Long id, AprobarLiquidacionRequest req,
                                        UsuarioPrincipal admin) {
        LiquidacionCaja liquidacion = liquidacionRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("LiquidacionCaja", id));

        if (!EstadoLiquidacion.PENDIENTE.equals(liquidacion.getEstado())
                && !EstadoLiquidacion.OBSERVADO.equals(liquidacion.getEstado())) {
            throw new IllegalStateException(
                    "No se puede procesar una liquidación en estado: " + liquidacion.getEstado());
        }

        Usuario aprobador = usuarioRepo.findById(admin.getId()).orElseThrow();

        liquidacion.setMontoDepositado(req.montoDepositado());
        liquidacion.setEstado(req.nuevoEstado());
        liquidacion.setAprobadoPor(aprobador);
        liquidacion.setFechaAprobacion(Instant.now());
        if (req.observaciones() != null) {
            liquidacion.setObservaciones(req.observaciones());
        }

        liquidacion = liquidacionRepo.save(liquidacion);
        // MySQL recalcula: diferencia = monto_depositado - monto_total_esperado

        // Si se aprueba: vincular las ventas del periodo a esta liquidación
        if (EstadoLiquidacion.APROBADO.equals(req.nuevoEstado())) {
            Instant desde = liquidacion.getPeriodoInicio().atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant hasta = liquidacion.getPeriodoFin().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

            int ventasVinculadas = ventaRepo.asignarLiquidacion(
                    liquidacion.getVendedor().getId(), liquidacion, desde, hasta);

            log.info("Liquidación {} aprobada: {} ventas vinculadas | diferencia={}",
                    id, ventasVinculadas, liquidacion.getDiferencia());
        }

        return mapper.toLiquidacionResponse(liquidacion);
    }
}
