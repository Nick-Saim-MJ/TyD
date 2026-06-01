package com.tyd.kitprepago.modulo_auth.entity;

import com.tyd.kitprepago.shared.security.Rol;
import com.tyd.kitprepago.shared.security.UsuarioSecurityProjection;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entidad principal del sistema de autenticación.
 *
 * Implementa UsuarioSecurityProjection para que UserDetailsServiceImpl
 * del módulo shared pueda leer los datos de seguridad sin depender
 * directamente de esta clase JPA.
 *
 * Campos de seguridad:
 *  - intentosFallidos: se incrementa en cada login fallido
 *  - bloqueadoHasta:   timestamp de desbloqueo automático (NULL = libre)
 *  - ultimoLogin:      para reportes de auditoría del contador
 */
@Entity
@Table(name = "usuarios")
@SQLRestriction("deleted_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario implements UsuarioSecurityProjection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false, length = 255)
    private String nombreCompleto;

    @Column(length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /**
     * NULL para ADMIN y JEFE_ALMACEN (operan en múltiples zonas).
     * Para los demás roles apunta a la zona de operación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private Zona zona;

    /**
     * NULL para ADMIN, JEFE_ALMACEN y CONTADOR.
     * Para ALMACENERO y VENDEDOR apunta a su sucursal de trabajo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Builder.Default
    private Boolean activo = true;

    // ── Campos de seguridad ──────────────────────────────────
    @Column(name = "intentos_fallidos", nullable = false)
    @Builder.Default
    private Integer intentosFallidos = 0;

    /** NULL = cuenta libre. Si isAfter(now) = cuenta bloqueada */
    @Column(name = "bloqueado_hasta")
    private Instant bloqueadoHasta;

    @Column(name = "ultimo_login")
    private Instant ultimoLogin;

    // ── Auditoría ────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private Usuario creadoPor;

    // ── Implementación de UsuarioSecurityProjection ──────────

    @Override public Long getZonaId() {
        return zona != null ? zona.getId() : null;
    }

    @Override public Long getSucursalId() {
        return sucursal != null ? sucursal.getId() : null;
    }

    @Override public boolean isActivo() {
        return Boolean.TRUE.equals(activo);
    }

    @Override public int getIntentosFallidos() {
        return intentosFallidos != null ? intentosFallidos : 0;
    }
}
