package com.tyd.kitprepago.modulo_reportes.mapper;

import com.tyd.kitprepago.modulo_reportes.dto.response.*;
import com.tyd.kitprepago.modulo_reportes.entity.KardexMensual;
import com.tyd.kitprepago.shared.audit.AuditLog;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportesMapper {

    // ── KARDEX ───────────────────────────────────────────────────────────────

    @Mapping(source = "sucursal.id",             target = "sucursalId")
    @Mapping(source = "sucursal.nombre",         target = "sucursalNombre")
    @Mapping(source = "sucursal.zona.nombre",    target = "zonaNombre")
    @Mapping(source = "producto.id",             target = "productoId")
    @Mapping(source = "producto.nombre",         target = "productoNombre")
    @Mapping(source = "generadoPor.nombreCompleto", target = "generadoPorNombre")
    KardexResponse toKardexResponse(KardexMensual k);

    List<KardexResponse> toKardexResponseList(List<KardexMensual> list);

    // ── AUDIT LOG ─────────────────────────────────────────────────────────────

    AuditLogResponse toAuditLogResponse(AuditLog a);

    List<AuditLogResponse> toAuditLogResponseList(List<AuditLog> list);
}
