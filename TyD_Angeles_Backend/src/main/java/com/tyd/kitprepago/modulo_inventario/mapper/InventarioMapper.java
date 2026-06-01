package com.tyd.kitprepago.modulo_inventario.mapper;

import com.tyd.kitprepago.modulo_inventario.dto.response.*;
import com.tyd.kitprepago.modulo_inventario.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventarioMapper {

    // ── MODELO KIT ───────────────────────────────────────────────────────────
    ModeloKitResponse toModeloKitResponse(ModeloKit m);
    List<ModeloKitResponse> toModeloKitResponseList(List<ModeloKit> list);

    // ── PRODUCTO ─────────────────────────────────────────────────────────────
    ProductoResponse toProductoResponse(Producto p);

    // ── LOTE ─────────────────────────────────────────────────────────────────
    @Mapping(source = "zona.id",                    target = "zonaId")
    @Mapping(source = "zona.nombre",                target = "zonaNombre")
    @Mapping(source = "zona.codigoZona",            target = "zonaCodigoDirecTV")
    @Mapping(source = "sucursalRecepcion.id",        target = "sucursalRecepcionId")
    @Mapping(source = "sucursalRecepcion.nombre",    target = "sucursalRecepcionNombre")
    @Mapping(source = "usuarioRegistro.nombreCompleto", target = "usuarioRegistroNombre")
    LoteResponse toLoteResponse(Lote l);
    List<LoteResponse> toLoteResponseList(List<Lote> lotes);

    // ── ITEM KIT ─────────────────────────────────────────────────────────────
    @Mapping(source = "lote.id",                    target = "loteId")
    @Mapping(source = "lote.numeroPedido",           target = "numeroPedido")
    @Mapping(source = "lote.numeroOperacion",        target = "numeroOperacion")
    @Mapping(source = "producto.id",                target = "productoId")
    @Mapping(source = "producto.nombre",            target = "productoNombre")
    @Mapping(source = "modeloKit.id",               target = "modeloKitId")
    @Mapping(source = "modeloKit.codigo",           target = "modeloKitCodigo")
    @Mapping(source = "modeloKit.tieneDeco",        target = "tieneDeco")
    @Mapping(source = "sucursalActual.id",          target = "sucursalActualId")
    @Mapping(source = "sucursalActual.nombre",      target = "sucursalActualNombre")
    @Mapping(source = "sucursalActual.zona.id",     target = "zonaId")
    @Mapping(source = "sucursalActual.zona.nombre", target = "zonaNombre")
    @Mapping(source = "custodioActual.nombreCompleto", target = "custodioActualNombre")
    ItemKitResponse toItemKitResponse(ItemKit ik);
    List<ItemKitResponse> toItemKitResponseList(List<ItemKit> items);

    // ── HISTORIAL ────────────────────────────────────────────────────────────
    @Mapping(source = "sucursalAnterior.nombre",    target = "sucursalAnteriorNombre")
    @Mapping(source = "sucursalNueva.nombre",       target = "sucursalNuevaNombre")
    @Mapping(source = "custodioAnterior.nombreCompleto", target = "custodioAnteriorNombre")
    @Mapping(source = "custodioNuevo.nombreCompleto",    target = "custodioNuevoNombre")
    @Mapping(source = "registradoPor.nombreCompleto",    target = "registradoPorNombre")
    HistorialCustodioResponse toHistorialResponse(HistorialCustodio h);
    List<HistorialCustodioResponse> toHistorialResponseList(List<HistorialCustodio> list);
}
