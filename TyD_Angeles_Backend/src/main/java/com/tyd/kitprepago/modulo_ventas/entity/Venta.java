package com.tyd.kitprepago.modulo_ventas.entity;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registro de venta al consumidor final.
 *
 * PROTECCIÓN CONTRA DOBLE VENTA:
 * La columna `item_kit_activo` es GENERATED ALWAYS AS en MySQL:
 *   IF(estado = 'ACTIVA', item_kit_id, NULL)
 * Más un UNIQUE INDEX sobre item_kit_activo.
 *
 * Resultado:
 *  - Dos ventas ACTIVAS del mismo kit → MySQL rechaza (UNIQUE violation)
 *  - Una venta ANULADA libera el UNIQUE → el kit puede venderse de nuevo
 *  - Múltiples ventas ANULADAS del mismo kit → permitido (NULL no es UNIQUE en MySQL)
 *
 * JPA declara item_kit_activo como insertable=false, updatable=false
 * porque MySQL lo calcula automáticamente.
 *
 * ANULACIÓN:
 * Al anular: estado → ANULADA, kit → DISPONIBLE en sucursal anterior.
 * item_kit_activo se vuelve NULL automáticamente (calculado por MySQL).
 */
@Entity
@Table(name = "ventas",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_kit_venta_activa",
        columnNames = "item_kit_activo"   // UNIQUE sobre la columna generada
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Venta {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_kit_id", nullable = false)
    private ItemKit itemKit;

    /**
     * Columna generada en MySQL: IF(estado='ACTIVA', item_kit_id, NULL).
     * JPA no la escribe — MySQL la gestiona.
     * El UNIQUE INDEX sobre ella garantiza que no puede haber dos ventas
     * ACTIVAS del mismo kit simultáneamente.
     */
    @Column(name = "item_kit_activo", insertable = false, updatable = false)
    private Long itemKitActivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_venta_id", nullable = false)
    private Sucursal sucursalVenta;

    @Column(name = "monto_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoVenta;

    /** Se actualiza al cerrar la liquidación del periodo */
    @Column(name = "monto_liquidado", precision = 10, scale = 2)
    private BigDecimal montoLiquidado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CondicionVenta condicion;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;          // EFECTIVO | YAPE | PLIN | TRANSFERENCIA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.ACTIVA;

    /** Se asigna al cerrar la liquidación del periodo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liquidacion_id")
    private LiquidacionCaja liquidacion;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulada_por_id")
    private Usuario anuladaPor;

    @Column(name = "fecha_anulacion")
    private Instant fechaAnulacion;

    /** Quien registró la venta en el sistema (puede diferir del vendedor) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private Usuario creadoPor;

    @CreationTimestamp
    @Column(name = "fecha_venta", updatable = false)
    private Instant fechaVenta;

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isActiva() {
        return EstadoVenta.ACTIVA.equals(this.estado);
    }
}
