package com.tyd.kitprepago.modulo_ventas.mapper;

import com.tyd.kitprepago.modulo_ventas.dto.response.*;
import com.tyd.kitprepago.modulo_ventas.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VentasMapper {

    // ── CLIENTE ──────────────────────────────────────────────────────────────

    @Mapping(target = "nombreCompleto",
             expression = "java(c.getNombres() + \" \" + c.getApellidos())")
    ClienteResponse toClienteResponse(Cliente c);

    List<ClienteResponse> toClienteResponseList(List<Cliente> clientes);

    // ── VENTA ────────────────────────────────────────────────────────────────

    @Mapping(source = "itemKit.id",                          target = "itemKitId")
    @Mapping(source = "itemKit.serieMaestro",                target = "serieMaestro")
    @Mapping(source = "itemKit.serieSim",                    target = "serieSim")
    @Mapping(source = "itemKit.producto.nombre",             target = "productoNombre")
    @Mapping(source = "cliente.id",                          target = "clienteId")
    @Mapping(source = "cliente.tipo",                        target = "clienteTipo")
    @Mapping(source = "cliente.dni",                         target = "clienteDni")
    @Mapping(target  = "clienteNombreCompleto",
             expression = "java(v.getCliente().getNombres() + \" \" + v.getCliente().getApellidos())")
    @Mapping(source = "vendedor.id",                         target = "vendedorId")
    @Mapping(source = "vendedor.nombreCompleto",             target = "vendedorNombre")
    @Mapping(source = "sucursalVenta.id",                    target = "sucursalVentaId")
    @Mapping(source = "sucursalVenta.nombre",                target = "sucursalVentaNombre")
    @Mapping(source = "sucursalVenta.zona.nombre",           target = "zonaNombre")
    @Mapping(source = "liquidacion.id",                      target = "liquidacionId")
    VentaResponse toVentaResponse(Venta v);

    List<VentaResponse> toVentaResponseList(List<Venta> ventas);

    // ── ACTIVACION ────────────────────────────────────────────────────────────

    @Mapping(source = "venta.id",                                  target = "ventaId")
    @Mapping(source = "venta.itemKit.serieSim",                    target = "serieSim")
    @Mapping(source = "venta.itemKit.serieMaestro",                target = "serieMaestro")
    @Mapping(source = "venta.itemKit.producto.nombre",             target = "productoNombre")
    @Mapping(source = "venta.vendedor.nombreCompleto",             target = "vendedorNombre")
    @Mapping(source = "venta.sucursalVenta.nombre",                target = "sucursalNombre")
    @Mapping(source = "venta.sucursalVenta.zona.nombre",           target = "zonaNombre")
    @Mapping(source = "venta.cliente.dni",                         target = "clienteDni")
    @Mapping(target  = "clienteNombreCompleto",
             expression = "java(a.getVenta().getCliente().getNombres() + \" \" + a.getVenta().getCliente().getApellidos())")
    ActivacionResponse toActivacionResponse(Activacion a);

    List<ActivacionResponse> toActivacionResponseList(List<Activacion> activaciones);

    // ── LIQUIDACION ───────────────────────────────────────────────────────────

    @Mapping(source = "vendedor.id",                target = "vendedorId")
    @Mapping(source = "vendedor.nombreCompleto",    target = "vendedorNombre")
    @Mapping(source = "sucursal.id",                target = "sucursalId")
    @Mapping(source = "sucursal.nombre",            target = "sucursalNombre")
    @Mapping(source = "sucursal.zona.nombre",       target = "zonaNombre")
    @Mapping(source = "aprobadoPor.nombreCompleto", target = "aprobadoPorNombre")
    LiquidacionResponse toLiquidacionResponse(LiquidacionCaja lc);

    List<LiquidacionResponse> toLiquidacionResponseList(List<LiquidacionCaja> liquidaciones);
}
