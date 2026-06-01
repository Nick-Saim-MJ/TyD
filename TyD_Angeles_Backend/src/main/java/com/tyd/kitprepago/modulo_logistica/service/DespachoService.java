package com.tyd.kitprepago.modulo_logistica.service;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_inventario.entity.EstadoKit;
import com.tyd.kitprepago.modulo_inventario.entity.HistorialCustodio;
import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import com.tyd.kitprepago.modulo_inventario.entity.TipoEvento;
import com.tyd.kitprepago.modulo_inventario.repository.HistorialCustodioRepository;
import com.tyd.kitprepago.modulo_inventario.repository.ItemKitRepository;
import com.tyd.kitprepago.modulo_logistica.dto.request.ConfirmarRecepcionRequest;
import com.tyd.kitprepago.modulo_logistica.dto.request.CrearDespachoRequest;
import com.tyd.kitprepago.modulo_logistica.dto.request.ItemRecepcionRequest;
import com.tyd.kitprepago.modulo_logistica.dto.response.ConfirmarRecepcionResponse;
import com.tyd.kitprepago.modulo_logistica.dto.response.DespachoDetalleResponse;
import com.tyd.kitprepago.modulo_logistica.dto.response.DespachoResponse;
import com.tyd.kitprepago.modulo_logistica.entity.*;
import com.tyd.kitprepago.modulo_logistica.mapper.LogisticaMapper;
import com.tyd.kitprepago.modulo_logistica.repository.DespachoItemRepository;
import com.tyd.kitprepago.modulo_logistica.repository.DespachoRepository;
import com.tyd.kitprepago.shared.audit.Auditable;
import com.tyd.kitprepago.shared.audit.TipoAccion;
import com.tyd.kitprepago.shared.exception.DespachoEstadoInvalidoException;
import com.tyd.kitprepago.shared.exception.KitNoDisponibleException;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.exception.ZonaNoAutorizadaException;
import com.tyd.kitprepago.shared.security.Rol;
import com.tyd.kitprepago.shared.security.UsuarioPrincipal;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DespachoService {

    private final DespachoRepository despachoRepo;
    private final DespachoItemRepository despachoItemRepo;
    private final ItemKitRepository itemKitRepo;
    private final HistorialCustodioRepository historialRepo;
    private final LogisticaMapper mapper;
    private final ZonaContextHolder zonaCtx;
    private final JpaRepository<Sucursal, Long> sucursalRepo;
    private final JpaRepository<Usuario, Long> usuarioRepo;

    // ─────────────────────── CREAR DESPACHO ──────────────────────────────────

    /**
     * Crea el despacho, valida los kits y los marca como EN_TRANSITO.
     * Todo en una sola transacción: si cualquier kit falla, ninguno se mueve.
     */
    @Transactional
    @Auditable(tabla = "despachos", accion = TipoAccion.INSERT)
    public DespachoResponse crear(CrearDespachoRequest req, UsuarioPrincipal emisor) {

        Sucursal origen  = cargarSucursal(req.sucursalOrigenId());
        Sucursal destino = cargarSucursal(req.sucursalDestinoId());
        Usuario  usuario = cargarUsuario(emisor.getId());

        // ── Validación de zona ─────────────────────────────────────────────
        // ALMACENERO solo puede despachar dentro de su zona
        boolean esInterZona = !origen.getZona().getId().equals(destino.getZona().getId());
        if (esInterZona) {
            boolean puedeInterZona = emisor.getRol() == Rol.ADMIN
                    || emisor.getRol() == Rol.JEFE_ALMACEN;
            if (!puedeInterZona) {
                throw new ZonaNoAutorizadaException(
                    "Un ALMACENERO solo puede crear despachos dentro de su zona. " +
                    "Para traslados entre zonas, contacta al Jefe de Almacén.");
            }
        }

        // El emisor debe tener acceso a la zona de origen
        zonaCtx.validarAccesoZona(origen.getZona().getId());

        // ── Construir cabecera del despacho ────────────────────────────────
        Despacho despacho = Despacho.builder()
                .sucursalOrigen(origen)
                .sucursalDestino(destino)
                .usuarioEnvia(usuario)
                .guiaRemision(req.guiaRemision())
                .observaciones(req.observaciones())
                .estado(EstadoDespacho.EN_TRANSITO)
                .fechaDespacho(Instant.now())
                .build();

        despacho = despachoRepo.save(despacho);

        // ── Procesar cada kit — SELECT FOR UPDATE evita doble despacho ─────
        List<DespachoItem> despachoItems = new ArrayList<>();
        for (Long itemKitId : req.itemKitIds()) {
            ItemKit kit = itemKitRepo.findById(itemKitId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("ItemKit", itemKitId));

            // Verificar que el kit está disponible y en la sucursal origen
            if (!kit.isDisponible()) {
                throw new KitNoDisponibleException(kit.getSerieMaestro(), kit.getEstado().name());
            }
            if (!origen.getId().equals(kit.getSucursalActual().getId())) {
                throw new IllegalArgumentException(
                    "El kit " + kit.getSerieMaestro() + " no se encuentra en la sucursal origen " + origen.getNombre());
            }
            // Verificar que no está en otro despacho activo
            if (despachoItemRepo.existeKitEnDespachoActivo(itemKitId)) {
                throw new IllegalStateException(
                    "El kit " + kit.getSerieMaestro() + " ya está incluido en otro despacho activo");
            }

            // Marcar en tránsito
            kit.marcarEnTransito();
            itemKitRepo.save(kit);

            // Registrar en historial
            historialRepo.save(HistorialCustodio.builder()
                    .itemKit(kit)
                    .sucursalAnterior(origen)
                    .sucursalNueva(destino)
                    .custodioAnterior(kit.getCustodioActual())
                    .tipoEvento(TipoEvento.TRASLADO)
                    .motivo("Despacho " + (req.guiaRemision() != null ? req.guiaRemision() : "s/n")
                            + " → " + destino.getNombre())
                    .referenciaId(despacho.getId())
                    .referenciaTipo("DESPACHO")
                    .registradoPor(usuario)
                    .build());

            despachoItems.add(DespachoItem.builder()
                    .despacho(despacho)
                    .itemKit(kit)
                    .estadoItem(EstadoDespachoItem.ENVIADO)
                    .build());
        }

        despachoItemRepo.saveAll(despachoItems);
        despacho.setItems(despachoItems);

        log.info("Despacho {} creado: {} kits de {} → {} | inter-zona: {}",
                despacho.getId(), req.itemKitIds().size(),
                origen.getNombre(), destino.getNombre(), esInterZona);

        return mapper.toDespachoResponse(despacho);
    }

    // ─────────────────────── LISTADOS ────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DespachoResponse> listar() {
        Long zonaId = zonaCtx.getZonaIdFiltro().orElse(null);
        return mapper.toDespachoResponseList(despachoRepo.findConFiltroZona(zonaId));
    }

    @Transactional(readOnly = true)
    public DespachoDetalleResponse detalle(Long id) {
        Despacho despacho = despachoRepo.findByIdConItems(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Despacho", id));
        zonaCtx.validarAccesoZona(despacho.getSucursalOrigen().getZona().getId());
        return mapper.toDespachoDetalleResponse(despacho);
    }

    @Transactional(readOnly = true)
    public List<DespachoResponse> pendientesRecepcion(UsuarioPrincipal usuario) {
        Long sucursalId = usuario.getSucursalId();
        if (sucursalId == null) {
            // ADMIN o JEFE_ALMACEN sin sucursal fija: ver todos los pendientes de su zona
            Long zonaId = zonaCtx.getZonaIdFiltro().orElse(null);
            return mapper.toDespachoResponseList(despachoRepo.findConFiltroZona(zonaId)
                    .stream()
                    .filter(d -> d.getEstado() == EstadoDespacho.EN_TRANSITO)
                    .toList());
        }
        return mapper.toDespachoResponseList(
                despachoRepo.findPendientesRecepcionEnSucursal(sucursalId));
    }

    // ─────────────────────── CONFIRMAR RECEPCIÓN ─────────────────────────────

    /**
     * El endpoint más crítico del módulo.
     *
     * El receptor confirma kit por kit. Por cada kit:
     *  - RECIBIDO_OK:         → DISPONIBLE en destino + historial TRASLADO completado
     *  - RECIBIDO_DEFECTUOSO: → DEFECTUOSO en destino  + historial BAJA
     *  - NO_RECIBIDO:         → DISPONIBLE en origen   + historial TRASLADO revertido
     *
     * Si el request no incluye un kit del despacho, se marca NO_RECIBIDO automáticamente.
     * TODO EN UNA SOLA @Transactional: si algo falla, ningún kit se mueve.
     */
    @Transactional
    @Auditable(tabla = "despachos", accion = TipoAccion.UPDATE)
    public ConfirmarRecepcionResponse confirmarRecepcion(Long despachoId,
                                                          ConfirmarRecepcionRequest req,
                                                          UsuarioPrincipal receptor) {

        // Cargar despacho con todos sus items en una sola query
        Despacho despacho = despachoRepo.findByIdConItems(despachoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Despacho", despachoId));

        // Solo se puede confirmar si está EN_TRANSITO
        if (!despacho.estaEnTransito()) {
            throw new DespachoEstadoInvalidoException(
                    despachoId, despacho.getEstado().name(), "confirmar recepción de");
        }

        // El receptor debe pertenecer a la zona/sucursal destino
        Sucursal destino = despacho.getSucursalDestino();
        Sucursal origen  = despacho.getSucursalOrigen();
        zonaCtx.validarAccesoZona(destino.getZona().getId());

        Usuario usuarioReceptor = cargarUsuario(receptor.getId());

        // Indexar el request por itemKitId para acceso O(1)
        Map<Long, ItemRecepcionRequest> recepcionPorKit = req.items().stream()
                .collect(Collectors.toMap(ItemRecepcionRequest::itemKitId, Function.identity()));

        long okCount = 0, defectuosoCount = 0, noRecibidoCount = 0;

        // Procesar cada item del despacho
        for (DespachoItem despachoItem : despacho.getItems()) {
            ItemKit kit = despachoItem.getItemKit();
            ItemRecepcionRequest confirmacion = recepcionPorKit.get(kit.getId());

            // Si no viene en el request → NO_RECIBIDO
            EstadoDespachoItem estadoRecepcion = confirmacion != null
                    ? confirmacion.estadoRecepcion()
                    : EstadoDespachoItem.NO_RECIBIDO;

            String observacion = confirmacion != null ? confirmacion.observacion() : null;

            switch (estadoRecepcion) {
                case RECIBIDO_OK -> {
                    // Kit llega bien → disponible en la sucursal destino
                    kit.marcarDisponibleEn(destino, usuarioReceptor);
                    historialRepo.save(historial(kit, origen, destino,
                            null, usuarioReceptor,
                            TipoEvento.TRASLADO,
                            "Recibido OK en " + destino.getNombre(),
                            despachoId));
                    okCount++;
                }
                case RECIBIDO_DEFECTUOSO -> {
                    // Kit llega dañado → defectuoso, queda en destino
                    kit.setEstado(EstadoKit.DEFECTUOSO);
                    kit.setSucursalActual(destino);
                    historialRepo.save(historial(kit, origen, destino,
                            null, usuarioReceptor,
                            TipoEvento.BAJA,
                            "Defectuoso al recibir: " + (observacion != null ? observacion : "sin detalle"),
                            despachoId));
                    defectuosoCount++;
                }
                case NO_RECIBIDO -> {
                    // Kit no llegó → vuelve a disponible en el origen
                    kit.marcarDisponibleEn(origen, kit.getCustodioActual());
                    historialRepo.save(historial(kit, destino, origen,
                            null, usuarioReceptor,
                            TipoEvento.TRASLADO,
                            "No recibido — devuelto a " + origen.getNombre(),
                            despachoId));
                    noRecibidoCount++;
                }
                default -> { /* ENVIADO no debería llegar aquí */ }
            }

            // Actualizar el despacho_item
            despachoItem.setEstadoItem(estadoRecepcion);
            despachoItem.setObservacion(observacion);
            itemKitRepo.save(kit);
        }

        // Actualizar cabecera del despacho
        EstadoDespacho estadoFinal = despacho.calcularEstadoFinalRecepcion();
        despacho.setEstado(estadoFinal);
        despacho.setUsuarioRecibe(usuarioReceptor);
        despacho.setFechaRecepcion(Instant.now());
        if (req.observacionesGenerales() != null) {
            despacho.setObservaciones(req.observacionesGenerales());
        }
        despachoRepo.save(despacho);

        log.info("Despacho {} confirmado: OK={} DEFECTUOSO={} NO_RECIBIDO={} | estado={}",
                despachoId, okCount, defectuosoCount, noRecibidoCount, estadoFinal);

        String mensaje = String.format(
            "Recepción registrada. %d OK, %d defectuosos, %d no recibidos.",
            okCount, defectuosoCount, noRecibidoCount);

        return new ConfirmarRecepcionResponse(
                despachoId, estadoFinal, okCount, defectuosoCount, noRecibidoCount, mensaje);
    }

    // ─────────────────────── HELPERS PRIVADOS ────────────────────────────────

    private Sucursal cargarSucursal(Long id) {
        return sucursalRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", id));
    }

    private Usuario cargarUsuario(Long id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private HistorialCustodio historial(ItemKit kit, Sucursal desde, Sucursal hacia,
                                         Usuario custodioAnterior, Usuario registrador,
                                         TipoEvento tipo, String motivo, Long despachoId) {
        return HistorialCustodio.builder()
                .itemKit(kit)
                .sucursalAnterior(desde)
                .sucursalNueva(hacia)
                .custodioAnterior(custodioAnterior)
                .custodioNuevo(registrador)
                .tipoEvento(tipo)
                .motivo(motivo)
                .referenciaId(despachoId)
                .referenciaTipo("DESPACHO")
                .registradoPor(registrador)
                .build();
    }
}
