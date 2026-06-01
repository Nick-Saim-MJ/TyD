package com.tyd.kitprepago.modulo_reportes.service;

import com.tyd.kitprepago.modulo_reportes.dto.response.AuditLogResponse;
import com.tyd.kitprepago.modulo_reportes.mapper.ReportesMapper;
import com.tyd.kitprepago.modulo_reportes.repository.AuditLogReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogReadRepository auditRepo;
    private final ReportesMapper mapper;

    /**
     * Historial de cambios de un registro específico.
     * Caso de uso: el contador ve diferencia en monto_venta
     * y quiere saber quién lo modificó y cuándo.
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> historialRegistro(String tabla, Long registroId) {
        return mapper.toAuditLogResponseList(
                auditRepo.findByTablaNombreAndRegistroIdOrderByFechaDesc(tabla, registroId));
    }

    /**
     * Actividad de un usuario en un periodo.
     * Caso de uso: "¿qué cambios hizo Maricarmen el 28 de enero
     * cuando faltaron los S/10 en la liquidación?"
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> actividadUsuario(Long usuarioId, String tabla,
                                                     String mes) {
        Instant desde = null, hasta = null;
        if (mes != null && mes.matches("\\d{4}-\\d{2}")) {
            YearMonth ym = YearMonth.parse(mes);
            desde = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            hasta = ym.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        }
        return mapper.toAuditLogResponseList(
                auditRepo.findConFiltros(usuarioId, tabla, desde, hasta));
    }
}
