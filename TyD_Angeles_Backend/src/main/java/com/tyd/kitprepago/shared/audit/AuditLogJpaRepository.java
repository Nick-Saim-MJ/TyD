package com.tyd.kitprepago.shared.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ── Repositorio JPA base ──────────────────────────────────────────────────────
@Repository
interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTablaNombreAndRegistroIdOrderByFechaDesc(
            String tablaNombre, Long registroId);
}

// ── Wrapper con REQUIRES_NEW para garantizar persistencia independiente ───────

/**
 * No usar JpaRepository directamente desde AuditAspect.
 * Este wrapper asegura que el INSERT en audit_log ocurra en su propia
 * transacción, separada de la transacción del servicio auditado.
 *
 * Resultado: si la transacción del servicio hace rollback (ej: error en venta),
 * el registro de auditoría YA SE GUARDÓ y no se revierte.
 * Esto es correcto: queremos saber que se intentó la operación incluso si falló.
 */