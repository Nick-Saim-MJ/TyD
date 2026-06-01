package com.tyd.kitprepago.shared.audit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entidad de auditoría INMUTABLE.
 * NUNCA debe tener operaciones UPDATE ni DELETE sobre esta tabla.
 * Solo INSERT a través de AuditLogRepository.guardarEnTransaccionIndependiente()
 */
@Entity
@Table(name = "audit_log",
        indexes = {
                @Index(name = "idx_tabla_registro", columnList = "tabla_nombre, registro_id"),
                @Index(name = "idx_usuario_fecha",  columnList = "usuario_id, fecha"),
                @Index(name = "idx_fecha",          columnList = "fecha")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tabla_nombre", nullable = false, length = 100)
    private String tablaNombre;

    @Column(name = "registro_id")
    private Long registroId;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false)
    private TipoAccion accion;  // INSERT | UPDATE | DELETE

    @Column(name = "datos_anteriores", columnDefinition = "JSON")
    private String datosAnteriores;

    @Column(name = "datos_nuevos", columnDefinition = "JSON")
    private String datosNuevos;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "fecha", nullable = false, updatable = false)
    private Instant fecha;
}
