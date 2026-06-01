package com.tyd.kitprepago.shared.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogRepository {

    private final AuditLogJpaRepository jpa;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarEnTransaccionIndependiente(AuditLog entry) {
        jpa.save(entry);
    }

    // Solo para el endpoint GET /api/audit del ADMIN
    @Transactional(readOnly = true)
    public List<AuditLog> buscarPorTablaYRegistro(String tabla, Long registroId) {
        return jpa.findByTablaNombreAndRegistroIdOrderByFechaDesc(tabla, registroId);
    }
}
