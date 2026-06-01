package com.tyd.kitprepago.modulo_logistica.mapper;

import com.tyd.kitprepago.modulo_logistica.dto.response.DespachoDetalleResponse;
import com.tyd.kitprepago.modulo_logistica.dto.response.DespachoItemResponse;
import com.tyd.kitprepago.modulo_logistica.dto.response.DespachoResponse;
import com.tyd.kitprepago.modulo_logistica.entity.Despacho;
import com.tyd.kitprepago.modulo_logistica.entity.DespachoItem;
import com.tyd.kitprepago.modulo_logistica.entity.EstadoDespachoItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LogisticaMapper {

    // ── DESPACHO ITEM ────────────────────────────────────────────────────────

    @Mapping(source = "itemKit.id",                          target = "itemKitId")
    @Mapping(source = "itemKit.serieMaestro",                target = "serieMaestro")
    @Mapping(source = "itemKit.serieSim",                    target = "serieSim")
    @Mapping(source = "itemKit.serieDeco",                   target = "serieDeco")
    @Mapping(source = "itemKit.producto.nombre",             target = "productoNombre")
    @Mapping(source = "itemKit.sucursalActual.nombre",       target = "sucursalActualNombre")
    DespachoItemResponse toDespachoItemResponse(DespachoItem di);

    List<DespachoItemResponse> toDespachoItemResponseList(List<DespachoItem> items);

    // ── DESPACHO (listado) ────────────────────────────────────────────────────

    @Mapping(source = "sucursalOrigen.id",           target = "sucursalOrigenId")
    @Mapping(source = "sucursalOrigen.nombre",       target = "sucursalOrigenNombre")
    @Mapping(source = "sucursalOrigen.zona.id",      target = "zonaOrigenId")
    @Mapping(source = "sucursalOrigen.zona.nombre",  target = "zonaOrigenNombre")
    @Mapping(source = "sucursalDestino.id",          target = "sucursalDestinoId")
    @Mapping(source = "sucursalDestino.nombre",      target = "sucursalDestinoNombre")
    @Mapping(source = "sucursalDestino.zona.id",     target = "zonaDestinoId")
    @Mapping(source = "sucursalDestino.zona.nombre", target = "zonaDestinoNombre")
    @Mapping(source = "usuarioEnvia.nombreCompleto", target = "usuarioEnviaNombre")
    @Mapping(source = "usuarioRecibe.nombreCompleto",target = "usuarioRecibeNombre")
    @Mapping(target = "totalItems",
             expression = "java(d.getItems() != null ? d.getItems().size() : 0)")
    @Mapping(target = "esInterZona",
             expression = "java(d.getSucursalOrigen().getZona().getId() != d.getSucursalDestino().getZona().getId())")
    DespachoResponse toDespachoResponse(Despacho d);

    List<DespachoResponse> toDespachoResponseList(List<Despacho> despachos);

    // ── DESPACHO DETALLE ──────────────────────────────────────────────────────

    @Mapping(source = "sucursalOrigen.id",           target = "sucursalOrigenId")
    @Mapping(source = "sucursalOrigen.nombre",       target = "sucursalOrigenNombre")
    @Mapping(source = "sucursalOrigen.zona.nombre",  target = "zonaOrigenNombre")
    @Mapping(source = "sucursalDestino.id",          target = "sucursalDestinoId")
    @Mapping(source = "sucursalDestino.nombre",      target = "sucursalDestinoNombre")
    @Mapping(source = "sucursalDestino.zona.nombre", target = "zonaDestinoNombre")
    @Mapping(source = "usuarioEnvia.nombreCompleto", target = "usuarioEnviaNombre")
    @Mapping(source = "usuarioRecibe.nombreCompleto",target = "usuarioRecibeNombre")
    @Mapping(target = "esInterZona",
             expression = "java(d.getSucursalOrigen().getZona().getId() != d.getSucursalDestino().getZona().getId())")
    @Mapping(target = "totalEnviados",
             expression = "java(contarPorEstado(d, com.tyd.kitprepago.modulo_logistica.entity.EstadoDespachoItem.ENVIADO))")
    @Mapping(target = "totalRecibidosOk",
             expression = "java(contarPorEstado(d, com.tyd.kitprepago.modulo_logistica.entity.EstadoDespachoItem.RECIBIDO_OK))")
    @Mapping(target = "totalDefectuosos",
             expression = "java(contarPorEstado(d, com.tyd.kitprepago.modulo_logistica.entity.EstadoDespachoItem.RECIBIDO_DEFECTUOSO))")
    @Mapping(target = "totalNoRecibidos",
             expression = "java(contarPorEstado(d, com.tyd.kitprepago.modulo_logistica.entity.EstadoDespachoItem.NO_RECIBIDO))")
    DespachoDetalleResponse toDespachoDetalleResponse(Despacho d);

    // Helper para los contadores — llamado desde las expresiones de MapStruct
    default long contarPorEstado(Despacho d, EstadoDespachoItem estado) {
        if (d.getItems() == null) return 0;
        return d.getItems().stream()
                .filter(i -> estado.equals(i.getEstadoItem()))
                .count();
    }
}
