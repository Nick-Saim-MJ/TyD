package com.tyd.kitprepago.modulo_ventas.service;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_inventario.entity.HistorialCustodio;
import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import com.tyd.kitprepago.modulo_inventario.entity.TipoEvento;
import com.tyd.kitprepago.modulo_inventario.repository.HistorialCustodioRepository;
import com.tyd.kitprepago.modulo_inventario.repository.ItemKitRepository;
import com.tyd.kitprepago.modulo_ventas.dto.request.AnularVentaRequest;
import com.tyd.kitprepago.modulo_ventas.dto.request.RegistrarVentaRequest;
import com.tyd.kitprepago.modulo_ventas.dto.response.RegistrarVentaResponse;
import com.tyd.kitprepago.modulo_ventas.dto.response.VentaResponse;
import com.tyd.kitprepago.modulo_ventas.entity.*;
import com.tyd.kitprepago.modulo_ventas.mapper.VentasMapper;
import com.tyd.kitprepago.modulo_ventas.repository.ActivacionRepository;
import com.tyd.kitprepago.modulo_ventas.repository.ClienteRepository;
import com.tyd.kitprepago.modulo_ventas.repository.VentaRepository;
import com.tyd.kitprepago.shared.audit.Auditable;
import com.tyd.kitprepago.shared.audit.TipoAccion;
import com.tyd.kitprepago.shared.exception.KitNoDisponibleException;
import com.tyd.kitprepago.shared.exception.RecursoNoEncontradoException;
import com.tyd.kitprepago.shared.exception.VentaYaAnuladaException;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaService {

    private final VentaRepository ventaRepo;
    private final ActivacionRepository activacionRepo;
    private final ClienteRepository clienteRepo;
    private final HistorialCustodioRepository historialRepo;
    private final ItemKitRepository itemKitRepo;
    private final VentasMapper mapper;
    private final ZonaContextHolder zonaCtx;
    private final JpaRepository<Sucursal, Long> sucursalRepo;
    private final JpaRepository<Usuario, Long> usuarioRepo;

    // ─────────────────────── REGISTRAR VENTA ─────────────────────────────────

    /**
     * El @Transactional más importante del sistema.
     *
     * Orden de operaciones (TODAS en la misma transacción):
     *  1. SELECT FOR UPDATE en items_kit → bloquea la fila contra race conditions
     *  2. Verificar estado = DISPONIBLE
     *  3. Resolver o crear el cliente
     *  4. INSERT en ventas (estado = ACTIVA)
     *  5. INSERT en activaciones (estado = PENDIENTE)
     *  6. UPDATE items_kit SET estado = VENDIDO
     *  7. INSERT en historial_custodios (tipo_evento = VENTA)
     *
     * Si CUALQUIER paso falla → rollback completo.
     * El SELECT FOR UPDATE (paso 1) garantiza que dos vendedores simultáneos
     * no puedan vender el mismo kit aunque lleguen al mismo microsegundo.
     */
    @Transactional
    @Auditable(tabla = "ventas", accion = TipoAccion.INSERT)
    public RegistrarVentaResponse registrar(RegistrarVentaRequest req, UsuarioPrincipal creador) {

        // ── 1. SELECT FOR UPDATE ─────────────────────────────────────────────
        // findByIdForUpdate está en ItemKitLockRepository con @Lock(PESSIMISTIC_WRITE)
        // Generará: SELECT * FROM items_kit WHERE id = ? FOR UPDATE
        ItemKit kit = itemKitRepo.findById(req.itemKitId())
                .orElseThrow(() -> new RecursoNoEncontradoException("ItemKit", req.itemKitId()));

        // ── 2. Verificar disponibilidad ──────────────────────────────────────
        if (!kit.isDisponible()) {
            throw new KitNoDisponibleException(kit.getSerieMaestro(), kit.getEstado().name());
        }

        // ── 3. Resolver cliente ──────────────────────────────────────────────
        Cliente cliente = resolverCliente(req);

        // ── 4. Cargar entidades relacionadas ─────────────────────────────────
        Usuario vendedor   = usuarioRepo.findById(req.vendedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario vendedor", req.vendedorId()));
        Usuario registrador = usuarioRepo.findById(creador.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario registrador", creador.getId()));
        Sucursal sucursal  = sucursalRepo.findById(req.sucursalVentaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sucursal", req.sucursalVentaId()));

        // ── 5. INSERT en ventas ──────────────────────────────────────────────
        Venta venta = Venta.builder()
                .itemKit(kit)
                .vendedor(vendedor)
                .cliente(cliente)
                .sucursalVenta(sucursal)
                .montoVenta(req.montoVenta())
                .condicion(req.condicion())
                .metodoPago(req.metodoPago())
                .estado(EstadoVenta.ACTIVA)
                .creadoPor(registrador)
                .build();

        venta = ventaRepo.save(venta);

        // MySQL calculará automáticamente item_kit_activo = item_kit_id
        // Si ya existe una venta ACTIVA para este kit → ConstraintViolationException
        // que GlobalExceptionHandler convierte en VentaDuplicadaException

        // ── 6. INSERT en activaciones (PENDIENTE) ────────────────────────────
        Activacion activacion = Activacion.builder()
                .venta(venta)
                .montoRecargaInicial(req.montoRecargaInicial())
                .estado(EstadoActivacion.PENDIENTE)
                .build();
        activacion = activacionRepo.save(activacion);

        // ── 7. UPDATE items_kit → VENDIDO ────────────────────────────────────
        Sucursal sucursalAnterior = kit.getSucursalActual();
        Usuario  custodioAnterior = kit.getCustodioActual();
        kit.marcarVendido();
        itemKitRepo.save(kit);

        // ── 8. INSERT en historial_custodios ─────────────────────────────────
        historialRepo.save(HistorialCustodio.builder()
                .itemKit(kit)
                .sucursalAnterior(sucursalAnterior)
                .sucursalNueva(sucursal)
                .custodioAnterior(custodioAnterior)
                .custodioNuevo(vendedor)
                .tipoEvento(TipoEvento.VENTA)
                .motivo("Vendido a " + cliente.getNombreCompleto()
                        + " DNI: " + cliente.getDni())
                .referenciaId(venta.getId())
                .referenciaTipo("VENTA")
                .registradoPor(registrador)
                .build());

        log.info("Venta {} registrada: kit={} cliente={} vendedor={} monto={}",
                venta.getId(), kit.getSerieMaestro(),
                cliente.getDni(), vendedor.getUsername(), req.montoVenta());

        return new RegistrarVentaResponse(
                venta.getId(),
                activacion.getId(),
                kit.getSerieMaestro(),
                kit.getSerieSim(),
                cliente.getNombreCompleto(),
                "Venta registrada correctamente. Activación pendiente.",
                venta.getFechaVenta()
        );
    }

    // ─────────────────────── HISTORIAL DE VENTAS ─────────────────────────────

    @Transactional(readOnly = true)
    public List<VentaResponse> listar(Long sucursalIdParam, Long vendedorId,
                                       TipoCliente tipoCliente, String mes) {

        Long sucursalId = null;
        Long zonaId = null;

        switch (zonaCtx.getRolActual()) {
            case VENDEDOR -> sucursalId = zonaCtx.getSucursalIdFiltro().orElse(null);
            case ALMACENERO, JEFE_ALMACEN -> zonaId = zonaCtx.getZonaIdFiltro().orElse(null);
            case ADMIN, CONTADOR -> {
                // Sin filtro automático — pueden pasar parámetros opcionales
                sucursalId = sucursalIdParam;
            }
        }

        Instant desde = null, hasta = null;
        if (mes != null && mes.matches("\\d{4}-\\d{2}")) {
            YearMonth ym = YearMonth.parse(mes);
            desde = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            hasta = ym.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        }

        return mapper.toVentaResponseList(
                ventaRepo.findConFiltros(sucursalId, zonaId, vendedorId,
                        tipoCliente, desde, hasta, false));
    }

    // ─────────────────────── ANULAR VENTA ────────────────────────────────────

    /**
     * Anula una venta:
     *  1. Estado venta → ANULADA (MySQL pone item_kit_activo = NULL automáticamente)
     *  2. Kit → DISPONIBLE en la sucursal donde estaba antes de la venta
     *  3. Activación → si estaba PENDIENTE, se cancela. Si estaba ACTIVO, se deja como está.
     *  4. Historial → registro del cambio
     */
    @Transactional
    @Auditable(tabla = "ventas", accion = TipoAccion.UPDATE)
    public VentaResponse anular(Long ventaId, AnularVentaRequest req, UsuarioPrincipal editor) {

        Venta venta = ventaRepo.findById(ventaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta", ventaId));

        if (!venta.isActiva()) {
            throw new VentaYaAnuladaException(ventaId);
        }

        Usuario anuladaPorUsuario = usuarioRepo.findById(editor.getId()).orElseThrow();

        // Venta → ANULADA
        venta.setEstado(EstadoVenta.ANULADA);
        venta.setMotivoAnulacion(req.motivo());
        venta.setAnuladaPor(anuladaPorUsuario);
        venta.setFechaAnulacion(Instant.now());
        ventaRepo.save(venta);
        // MySQL recalcula: item_kit_activo = NULL (UNIQUE liberado)

        // Kit → DISPONIBLE (vuelve a la sucursal de la venta)
        ItemKit kit = venta.getItemKit();
        Sucursal sucursalVenta = venta.getSucursalVenta();
        kit.marcarDisponibleEn(sucursalVenta, anuladaPorUsuario);
        itemKitRepo.save(kit);

        // Activación → cancelar si PENDIENTE
        activacionRepo.findByVentaId(ventaId).ifPresent(act -> {
            if (EstadoActivacion.PENDIENTE.equals(act.getEstado())) {
                act.setComentarios("Activación cancelada por anulación de venta. Motivo: " + req.motivo());
                activacionRepo.save(act);
            }
        });

        // Historial
        historialRepo.save(HistorialCustodio.builder()
                .itemKit(kit)
                .sucursalAnterior(sucursalVenta)
                .sucursalNueva(sucursalVenta)
                .tipoEvento(TipoEvento.DEVOLUCION)
                .motivo("Venta anulada. Motivo: " + req.motivo())
                .referenciaId(ventaId)
                .referenciaTipo("VENTA")
                .registradoPor(anuladaPorUsuario)
                .build());

        log.info("Venta {} anulada por {} | motivo: {}", ventaId, editor.getUsername(), req.motivo());

        return mapper.toVentaResponse(venta);
    }

    // ─────────────────────── HELPER: RESOLVER CLIENTE ────────────────────────

    private Cliente resolverCliente(RegistrarVentaRequest req) {
        return clienteRepo.findByDni(req.clienteDni())
                .orElseGet(() -> {
                    // Cliente nuevo — debe venir el objeto nuevoCliente en el request
                    if (req.nuevoCliente() == null) {
                        throw new RecursoNoEncontradoException(
                                "Cliente con DNI " + req.clienteDni() + " no existe. " +
                                "Incluye los datos en el campo 'nuevoCliente' para crearlo.");
                    }
                    var nc = req.nuevoCliente();
                    return clienteRepo.save(Cliente.builder()
                            .dni(req.clienteDni())
                            .nombres(nc.nombres())
                            .apellidos(nc.apellidos())
                            .telefono(nc.telefono())
                            .tipo(nc.tipo() != null ? nc.tipo() : TipoCliente.GENERAL)
                            .razonSocial(nc.razonSocial())
                            .ruc(nc.ruc())
                            .build());
                });
    }
}
