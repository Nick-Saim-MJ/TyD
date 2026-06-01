package com.tyd.kitprepago.modulo_inventario.entity;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Línea de vida de cada kit. INMUTABLE — nunca UPDATE ni DELETE.
 *
 * Permite responder en cualquier momento:
 *  - ¿Dónde estaba el kit K10RBA50E0093 el 15 de enero?
 *  - ¿Quién tenía en custodia el kit antes de que se vendiera?
 *  - ¿Cuántos kits recepcionó el almacenero Javier en febrero?
 *
 * referencia_id + referencia_tipo son polimórficos (no FK en BD):
 *  - DESPACHO → id del despacho que originó el traslado
 *  - VENTA    → id de la venta que consumió el kit
 *  - AJUSTE_MANUAL → null (cambio manual por admin)
 */
@Entity
@Table(name = "historial_custodios",
    indexes = {
        @Index(name = "idx_item_fecha", columnList = "item_kit_id, fecha_evento")
    }
)
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialCustodio {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_kit_id", nullable = false)
    private ItemKit itemKit;

    // ── Ubicación anterior ───────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_anterior_id")
    private Sucursal sucursalAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodio_anterior_id")
    private Usuario custodioAnterior;

    // ── Ubicación nueva ──────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_nueva_id")
    private Sucursal sucursalNueva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodio_nuevo_id")
    private Usuario custodioNuevo;

    // ── Evento ───────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 20)
    private TipoEvento tipoEvento;

    @Column(length = 500)
    private String motivo;

    /** ID del despacho o venta que originó este movimiento. Puede ser null. */
    @Column(name = "referencia_id")
    private Long referenciaId;

    /** "DESPACHO" | "VENTA" | "AJUSTE_MANUAL" */
    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Usuario registradoPor;

    @CreationTimestamp
    @Column(name = "fecha_evento", updatable = false)
    private Instant fechaEvento;
}
