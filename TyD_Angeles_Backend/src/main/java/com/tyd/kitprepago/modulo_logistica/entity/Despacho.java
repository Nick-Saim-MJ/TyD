package com.tyd.kitprepago.modulo_logistica.entity;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de un traslado entre sucursales.
 *
 * Puede ser:
 *  - Almacén → Oficina (caso más común)
 *  - Oficina → Oficina (reasignación)
 *  - Almacén → Almacén de otra zona (solo ADMIN/JEFE_ALMACEN)
 *
 * El flujo de estados es:
 *   PREPARANDO → EN_TRANSITO → RECIBIDO | RECIBIDO_CON_OBSERVACIONES
 *                            ↘ CANCELADO (solo mientras PREPARANDO)
 *
 * Un despacho contiene N kits, cada uno representado por un DespachoItem.
 * La confirmación es kit por kit para poder registrar observaciones individuales.
 */
@Entity
@Table(name = "despachos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Despacho {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_origen_id", nullable = false)
    private Sucursal sucursalOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_destino_id", nullable = false)
    private Sucursal sucursalDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoDespacho estado = EstadoDespacho.PREPARANDO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_envia_id", nullable = false)
    private Usuario usuarioEnvia;

    /** Asignado al momento de confirmar recepción */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_recibe_id")
    private Usuario usuarioRecibe;

    /** Número de guía de remisión física — documento SUNAT */
    @Column(name = "guia_remision", length = 100)
    private String guiaRemision;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_despacho")
    private Instant fechaDespacho;

    @Column(name = "fecha_recepcion")
    private Instant fechaRecepcion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "despacho", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DespachoItem> items = new ArrayList<>();

    // ── Helpers de negocio ───────────────────────────────────────────────────

    public boolean estaEnTransito() {
        return EstadoDespacho.EN_TRANSITO.equals(this.estado);
    }

    public boolean estaPreparando() {
        return EstadoDespacho.PREPARANDO.equals(this.estado);
    }

    /**
     * Determina el estado final del despacho tras la confirmación.
     * Si todos los items están OK → RECIBIDO.
     * Si alguno tiene observación → RECIBIDO_CON_OBSERVACIONES.
     */
    public EstadoDespacho calcularEstadoFinalRecepcion() {
        boolean tieneObservaciones = items.stream()
                .anyMatch(i -> i.getEstadoItem() == EstadoDespachoItem.RECIBIDO_DEFECTUOSO
                            || i.getEstadoItem() == EstadoDespachoItem.NO_RECIBIDO);
        return tieneObservaciones
                ? EstadoDespacho.RECIBIDO_CON_OBSERVACIONES
                : EstadoDespacho.RECIBIDO;
    }

    /** Verifica si origen y destino son de zonas distintas */
    public boolean esInterZona() {
        return !sucursalOrigen.getZona().getId()
                .equals(sucursalDestino.getZona().getId());
    }
}
