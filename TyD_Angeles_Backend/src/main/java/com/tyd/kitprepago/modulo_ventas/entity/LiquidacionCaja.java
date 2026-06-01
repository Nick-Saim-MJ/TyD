package com.tyd.kitprepago.modulo_ventas.entity;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Liquidación de caja por vendedor y periodo.
 *
 * Flujo:
 *  1. Vendedor/Almacenero crea la liquidación (estado=PENDIENTE).
 *     El sistema calcula monto_total_esperado sumando ventas no liquidadas del periodo.
 *  2. Vendedor deposita el dinero y registra monto_depositado.
 *  3. ADMIN aprueba o rechaza.
 *
 * La columna `diferencia` es GENERATED ALWAYS AS en MySQL:
 *   diferencia = monto_depositado - monto_total_esperado
 * JPA la declara como insertable=false, updatable=false — MySQL la calcula sola.
 * Positivo = sobrante. Negativo = faltante (caso Maricarmen del Excel: -10.00).
 */
@Entity
@Table(name = "liquidaciones_caja")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LiquidacionCaja {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "monto_total_esperado", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoTotalEsperado = BigDecimal.ZERO;

    @Column(name = "monto_depositado", precision = 10, scale = 2)
    private BigDecimal montoDepositado;

    /**
     * GENERATED ALWAYS AS (monto_depositado - monto_total_esperado) STORED en MySQL.
     * JPA no lo escribe — MySQL lo calcula automáticamente.
     * insertable=false, updatable=false obligatorio para evitar error de JPA.
     */
    @Column(name = "diferencia", precision = 10, scale = 2,
            insertable = false, updatable = false)
    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoLiquidacion estado = EstadoLiquidacion.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por_id")
    private Usuario aprobadoPor;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_liquidacion")
    private Instant fechaLiquidacion;

    @Column(name = "fecha_aprobacion")
    private Instant fechaAprobacion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
