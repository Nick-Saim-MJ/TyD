package com.tyd.kitprepago.modulo_inventario.service;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_auth.entity.Zona;
import com.tyd.kitprepago.modulo_inventario.dto.request.CrearLoteRequest;
import com.tyd.kitprepago.modulo_inventario.dto.request.ItemKitIngresoRequest;
import com.tyd.kitprepago.modulo_inventario.dto.response.ItemKitResponse;
import com.tyd.kitprepago.modulo_inventario.dto.response.LoteResponse;
import com.tyd.kitprepago.modulo_inventario.entity.*;
import com.tyd.kitprepago.modulo_inventario.mapper.InventarioMapper;
import com.tyd.kitprepago.modulo_inventario.repository.*;
import com.tyd.kitprepago.shared.audit.Auditable;
import com.tyd.kitprepago.shared.audit.TipoAccion;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.exception.SerialDuplicadoException;
import com.tyd.kitprepago.shared.export.ColumnDefinition;
import com.tyd.kitprepago.shared.export.ExcelExportService;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoteService {

    private final LoteRepository loteRepo;
    private final ItemKitRepository itemKitRepo;
    private final InventarioMapper mapper;
    private final ZonaContextHolder zonaCtx;
    private final ExcelExportService excelService;
    private final JpaRepository<Zona, Long> zonaRepo;
    private final JpaRepository<Sucursal, Long> sucursalRepo;
    private final JpaRepository<Usuario, Long> usuarioRepo;
    private final HistorialCustodioRepository historialRepo;
    private final ProductoRepository productoRepo;
    private final ModeloKitRepository modeloKitRepo;


    @Transactional(readOnly = true)
    public List<LoteResponse> listar(Long zonaId, String periodo) {
        // 1. Determinar la zona: si el usuario manda una, úsala; si no, aplica el filtro de contexto
        Long zonaFiltro = zonaId != null ? zonaId : zonaCtx.getZonaIdFiltro().orElse(null);

        // 2. Procesar el periodo (copiado de tu lógica de exportación)
        LocalDate desde = null, hasta = null;
        if (periodo != null && periodo.matches("\\d{4}-\\d{2}")) {
            int y = Integer.parseInt(periodo.substring(0, 4));
            int m = Integer.parseInt(periodo.substring(5, 7));
            desde = LocalDate.of(y, m, 1);
            hasta = desde.plusMonths(1).minusDays(1);
        }

        // 3. Llamar al repositorio con los filtros correctos
        return mapper.toLoteResponseList(loteRepo.findConFiltros(zonaFiltro, desde, hasta));
    }

    @Transactional(readOnly = true)
    public List<ItemKitResponse> listarItemsDeLote(Long loteId) {
        Lote lote = loteRepo.findById(loteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote", loteId));
        zonaCtx.validarAccesoZona(lote.getZona().getId());
        return mapper.toItemKitResponseList(itemKitRepo.findByLoteId(loteId));
    }

    /**
     * Genera Excel de lotes con filtros opcionales.
     * Accesible para ADMIN, JEFE_ALMACEN y CONTADOR.
     */
    @Transactional(readOnly = true)
    public byte[] exportarExcel(Long zonaId, String periodo) {
        // Si zonaId no se pasa, ZonaContextHolder aplica el filtro del usuario
        Long zonaFiltro = zonaId != null ? zonaId
                : zonaCtx.getZonaIdFiltro().orElse(null);

        LocalDate desde = null, hasta = null;
        if (periodo != null && periodo.matches("\\d{4}-\\d{2}")) {
            int y = Integer.parseInt(periodo.substring(0, 4));
            int m = Integer.parseInt(periodo.substring(5, 7));
            desde = LocalDate.of(y, m, 1);
            hasta = desde.plusMonths(1).minusDays(1);
        }

        List<LoteResponse> datos = mapper.toLoteResponseList(
                loteRepo.findConFiltros(zonaFiltro, desde, hasta));

        List<ColumnDefinition<LoteResponse>> columnas = List.of(
                new ColumnDefinition<>("N° Pedido", LoteResponse::numeroPedido),
                new ColumnDefinition<>("N° Operación", LoteResponse::numeroOperacion),
                new ColumnDefinition<>("Zona", LoteResponse::zonaNombre),
                new ColumnDefinition<>("Cód. DirecTV", LoteResponse::zonaCodigoDirecTV),
                new ColumnDefinition<>("Sucursal Recep.", LoteResponse::sucursalRecepcionNombre),
                new ColumnDefinition<>("Cant. Esperada", LoteResponse::cantidadEsperada),
                new ColumnDefinition<>("Cant. Recibida", LoteResponse::cantidadRecibida),
                new ColumnDefinition<>("F. Pedido", LoteResponse::fechaPedido),
                new ColumnDefinition<>("F. Recepción", LoteResponse::fechaRecepcion),
                new ColumnDefinition<>("Registrado por", LoteResponse::usuarioRegistroNombre),
                new ColumnDefinition<>("Observaciones", LoteResponse::observaciones)
        );

        String titulo = "Lotes" + (periodo != null ? " - " + periodo : "");
        return excelService.exportar(titulo, datos, columnas);
    }

    @Transactional
    @Auditable(tabla = "lotes", accion = TipoAccion .INSERT)
    public LoteResponse crear(CrearLoteRequest req, UsuarioPrincipal admin) {
        if (loteRepo.existsByNumeroPedido(req.numeroPedido())) {
            throw new SerialDuplicadoException("N° Pedido ya existe: " + req.numeroPedido());
        }
        if (loteRepo.existsByNumeroOperacion(req.numeroOperacion())) {
            throw new SerialDuplicadoException("N° Operación ya existe: " + req.numeroOperacion());
        }

        Zona zona = zonaRepo.findById(req.zonaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Zona", req.zonaId()));
        Sucursal sucursal = sucursalRepo.findById(req.sucursalRecepcionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", req.sucursalRecepcionId()));
        Usuario registrador = usuarioRepo.findById(admin.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", admin.getId()));

        Lote lote = Lote.builder()
                .numeroPedido(req.numeroPedido())
                .numeroOperacion(req.numeroOperacion())
                .zona(zona)
                .sucursalRecepcion(sucursal)
                .cantidadEsperada(req.cantidadEsperada())
                .fechaPedido(req.fechaPedido())
                .fechaRecepcion(req.fechaRecepcion())
                .observaciones(req.observaciones())
                .usuarioRegistro(registrador)
                .build();

        lote = loteRepo.save(lote);

        // Registrar los kits que vienen en el payload (si los hay)
        if (req.items() != null && !req.items().isEmpty()) {
            ingresarKits(lote, req.items(), sucursal, registrador);
        }

        log.info("Lote creado: {} | zona: {} | {} kits",
                req.numeroPedido(), zona.getNombre(),
                req.items() != null ? req.items().size() : 0);

        return mapper.toLoteResponse(lote);
    }

    private void ingresarKits(Lote lote, List<ItemKitIngresoRequest> items,
                              Sucursal sucursal, Usuario registrador) {
        for (ItemKitIngresoRequest item : items) {
            validarSerialUnico(item.serieMaestro(), item.serieSim(), item.serieDeco());

            ItemKit kit = ItemKit.builder()
                    .lote(lote)
                    .producto(productoRef(item.productoId()))
                    .modeloKit(item.modeloKitId() != null ? modeloRef(item.modeloKitId()) : null)
                    .serieMaestro(item.serieMaestro())
                    .serieSim(item.serieSim())
                    .serieDeco(item.serieDeco())
                    .estado(EstadoKit.DISPONIBLE)
                    .sucursalActual(sucursal)
                    .custodioActual(registrador)
                    .build();

            kit = itemKitRepo.save(kit);

            // Insertar en historial — tipo INGRESO
            historialRepo.save(HistorialCustodio.builder()
                    .itemKit(kit)
                    .sucursalNueva(sucursal)
                    .custodioNuevo(registrador)
                    .tipoEvento(TipoEvento.INGRESO)
                    .motivo("Recepción lote N° " + lote.getNumeroPedido())
                    .referenciaId(lote.getId())
                    .referenciaTipo("LOTE")
                    .registradoPor(registrador)
                    .build());
        }
        lote.setCantidadRecibida(items.size());
        loteRepo.save(lote);
    }

    private void validarSerialUnico(String maestro, String sim, String deco) {
        if (itemKitRepo.existsBySerieMaestro(maestro))
            throw new SerialDuplicadoException("serie_maestro: " + maestro);
        if (itemKitRepo.existsBySerieSim(sim))
            throw new SerialDuplicadoException("serie_sim: " + sim);
        if (deco != null && itemKitRepo.existsBySerieDecoAndSerieDecoIsNotNull(deco))
            throw new SerialDuplicadoException("serie_deco: " + deco);
    }

    private Producto productoRef(Long id) {
        // getReferenceById no hace un SELECT, solo crea el vínculo para la FK
        return productoRepo.getReferenceById(id);
    }

    private ModeloKit modeloRef(Long id) {

        return modeloKitRepo.getReferenceById(id);
    }
}
