package com.tyd.kitprepago.modulo_reportes.repository;

import com.tyd.kitprepago.shared.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogReadRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Historial de cambios de un registro específico.
     * Para cuando el contador detecta una discrepancia.
     * Ej: GET /api/audit?tabla=ventas&registroId=42
     */
    List<AuditLog> findByTablaNombreAndRegistroIdOrderByFechaDesc(
            String tablaNombre, Long registroId);

    /**
     * Actividad de un usuario en un periodo — para investigaciones de admin.
     * Ej: "¿qué cambios hizo Maricarmen el día que faltaron los S/10?"
     */
    @Query("""

            SELECT a FROM AuditLog a
        WHERE (:usuarioId IS NULL OR a.usuarioId = :usuarioId)
          AND (:tabla     IS NULL OR a.tablaNombre = :tabla)
          AND (:desde     IS NULL OR a.fecha >= :desde)
          AND (:hasta     IS NULL OR a.fecha <= :hasta)
        ORDER BY a.fecha DESC
        """)
    List<AuditLog> findConFiltros(
            @Param("usuarioId") Long usuarioId,
            @Param("tabla")     String tabla,
            @Param("desde") Instant desde,
            @Param("hasta")     Instant hasta
    );
}
