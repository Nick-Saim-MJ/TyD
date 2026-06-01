package com.tyd.kitprepago.modulo_inventario.dto.response;
public record ModeloKitResponse(Long id, String codigo, String nombre,
    String descripcion, Boolean tieneDeco, Boolean activo) {}
