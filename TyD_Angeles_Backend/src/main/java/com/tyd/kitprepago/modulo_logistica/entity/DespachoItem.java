package com.tyd.kitprepago.modulo_logistica.entity;

import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import jakarta.persistence.*;
import lombok.*;

/**
 * Detalle de un despacho: un kit incluido en el traslado.
 *
 * UNIQUE en (despacho_id, item_kit_id): un kit no puede aparecer
 * dos veces en el mismo despacho.
 *
 * El estado individual permite que al recepcionar se confirme
 * kit por kit: algunos pueden llegar OK, otros defectuosos, otros
 * no llegar — sin afectar la validez de los demás.
 */
@Entity
@Table(name = "despacho_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_despacho_item",
        columnNames = {"despacho_id", "item_kit_id"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DespachoItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "despacho_id", nullable = false)
    private Despacho despacho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_kit_id", nullable = false)
    private ItemKit itemKit;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_item", nullable = false, length = 25)
    @Builder.Default
    private EstadoDespachoItem estadoItem = EstadoDespachoItem.ENVIADO;

    /** Descripción del defecto, motivo de no llegada, etc. */
    @Column(length = 500)
    private String observacion;
}
