package com.tyd.kitprepago.modulo_reportes.service;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_inventario.entity.Producto;
import com.tyd.kitprepago.modulo_inventario.repository.ItemKitRepository;
import com.tyd.kitprepago.modulo_reportes.dto.response.GenerarKardexResponse;
import com.tyd.kitprepago.modulo_reportes.dto.response.KardexResponse;
import com.tyd.kitprepago.modulo_reportes.entity.KardexMensual;
import com.tyd.kitprepago.modulo_reportes.mapper.ReportesMapper;
import com.tyd.kitprepago.modulo_reportes.repository.KardexMensualRepository;
import com.tyd.kitprepago.shared.exception.PeriodoCerradoException;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KardexService {

    private final KardexMensualRepository kardexRepo;
    private final ItemKitRepository itemKitRepo;
    private final ReportesMapper mapper;
    private final ZonaContextHolder zonaCtx;
    private final JpaRepository<Sucursal, Long> sucursalRepo;
    private final JpaRepository<Producto, Long> productoRepo;
    private final JpaRepository<Usuario, Long> usuarioRepo;

    // ─────────────────────── CONSULTA ────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<KardexResponse> consultar(Long sucursalId, String periodo) {
        Long zonaFiltro = zonaCtx.getZonaIdFiltro().orElse(null);
        return mapper.toKardexResponseList(
                kardexRepo.findConFiltros(zonaFiltro, sucursalId, periodo));
    }

    // ─────────────────────── GENERAR ─────────────────────────────────────────

    /**
     * Genera el kardex para una sucursal y periodo calculando desde historial_custodios.
     *
     * Por cada producto que tuvo movimientos en el periodo:
     *  1. stock_inicio = stock_fin del periodo anterior (o 0 si es el primer kardex)
     *  2. total_ingresos = COUNT(historial WHERE sucursal_nueva=X AND evento IN INGRESO/TRASLADO)
     *  3. total_salidas  = COUNT(historial WHERE sucursal_anterior=X AND evento IN VENTA/BAJA/TRASLADO)
     *  4. total_liquidado = SUM(ventas.monto_venta WHERE sucursal=X AND periodo)
     *  5. stock_fin = GENERATED por MySQL (stock_inicio + ingresos - salidas)
     *
     * Si ya existe un kardex para esa sucursal/producto/periodo → lo actualiza.
     * Si está cerrado → lanza PeriodoCerradoException.
     */
    @Transactional
    public GenerarKardexResponse generar(Long sucursalId, String periodo,
                                          UsuarioPrincipal generador) {

        validarFormatoPeriodo(periodo);
        Sucursal sucursal = sucursalRepo.findById(sucursalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", sucursalId));
        zonaCtx.validarAccesoZona(sucursal.getZona().getId());

        YearMonth ym    = YearMonth.parse(periodo);
        Instant   desde = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant   hasta = ym.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        Usuario usuarioGen = usuarioRepo.findById(generador.getId()).orElseThrow();

        // Obtener todos los productos activos
        List<Producto> productos = productoRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .toList();

        List<KardexResponse> generados = new ArrayList<>();

        for (Producto producto : productos) {

            // Verificar si ya existe y si está cerrado
            var existente = kardexRepo.findBySucursalIdAndProductoIdAndPeriodo(
                    sucursalId, producto.getId(), periodo);

            if (existente.isPresent() && Boolean.TRUE.equals(existente.get().getCerrado())) {
                throw new PeriodoCerradoException(periodo + " (sucursal: " + sucursal.getNombre()
                        + ", producto: " + producto.getNombre() + ")");
            }

            // Calcular valores desde historial_custodios
            int stockInicio = kardexRepo.findStockFinPeriodoAnterior(
                    sucursalId, producto.getId(), periodo).orElse(0);

            int ingresos = kardexRepo.contarIngresosSucursal(
                    sucursalId, producto.getId(), desde, hasta);

            int salidas = kardexRepo.contarSalidasSucursal(
                    sucursalId, producto.getId(), desde, hasta);

            var totalLiquidado = kardexRepo.sumMontoVentasSucursal(
                    sucursalId, desde, hasta);

            // Solo persistir si hubo movimientos o si ya existía
            if (ingresos == 0 && salidas == 0 && stockInicio == 0 && existente.isEmpty()) {
                continue; // sin actividad para este producto en este periodo
            }

            KardexMensual kardex = existente.orElseGet(() -> KardexMensual.builder()
                    .sucursal(sucursal)
                    .producto(producto)
                    .periodo(periodo)
                    .build());

            kardex.setStockInicio(stockInicio);
            kardex.setTotalIngresos(ingresos);
            kardex.setTotalSalidas(salidas);
            kardex.setTotalLiquidado(totalLiquidado != null ? totalLiquidado : java.math.BigDecimal.ZERO);
            kardex.setGeneradoPor(usuarioGen);

            generados.add(mapper.toKardexResponse(kardexRepo.save(kardex)));
        }

        log.info("Kardex generado: sucursal={} periodo={} productos={}",
                sucursal.getNombre(), periodo, generados.size());

        return new GenerarKardexResponse(
                periodo,
                sucursal.getNombre(),
                generados.size(),
                generados,
                "Kardex generado para " + generados.size() + " producto(s). "
                + "Usa POST /api/kardex/{id}/cerrar para bloquear el periodo."
        );
    }

    // ─────────────────────── CERRAR PERIODO ──────────────────────────────────

    /**
     * Bloquea el kardex para auditoría del contador.
     * Una vez cerrado, el contador ve datos congelados e inmutables.
     * Solo ADMIN puede reabrir (cambiar cerrado = false).
     */
    @Transactional
    public KardexResponse cerrar(Long id) {
        KardexMensual kardex = kardexRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("KardexMensual", id));

        if (Boolean.TRUE.equals(kardex.getCerrado())) {
            throw new PeriodoCerradoException(kardex.getPeriodo() + " — ya está cerrado");
        }

        kardex.setCerrado(true);
        kardex = kardexRepo.save(kardex);

        log.info("Kardex {} cerrado: {}/{}", id,
                kardex.getSucursal().getNombre(), kardex.getPeriodo());

        return mapper.toKardexResponse(kardex);
    }

    @Transactional
    public KardexResponse reabrir(Long id) {
        KardexMensual kardex = kardexRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("KardexMensual", id));
        kardex.setCerrado(false);
        return mapper.toKardexResponse(kardexRepo.save(kardex));
    }

    // ─────────────────────── HELPER ──────────────────────────────────────────

    private void validarFormatoPeriodo(String periodo) {
        if (periodo == null || !periodo.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException(
                    "Formato de periodo inválido: '" + periodo + "'. Use YYYY-MM. Ej: 2025-03");
        }
    }
}
