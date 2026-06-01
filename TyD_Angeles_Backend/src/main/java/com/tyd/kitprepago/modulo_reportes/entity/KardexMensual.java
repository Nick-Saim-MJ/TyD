package com.tyd.kitprepago.modulo_reportes.entity;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_inventario.entity.Producto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Kardex mensual por sucursal y producto.
 *
 * Generado mediante POST /api/kardex/generar — calcula desde historial_custodios.
 * Una vez cerrado (cerrado = TRUE), ningún módulo puede modificarlo.
 *
 * stock_fin es GENERATED ALWAYS AS en MySQL:
 *   stock_inicio + total_ingresos - total_salidas
 * JPA lo declara insertable=false, updatable=false.
 *
 * total_liquidado: suma de montos_venta de ventas ACTIVAS registradas
 * en esa sucursal durante el periodo — para el contador.
 *
 * El UNIQUE KEY en (sucursal_id, producto_id, periodo) garantiza que
 * solo puede existir un kardex por sucursal/producto/mes.
 */
@Entity
@Table(name = "kardex_mensual",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_kardex_periodo",
        columnNames = {"sucursal_id", "producto_id", "periodo"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KardexMensual {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /** Formato YYYY-MM. Ej: 2025-01 */
    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(name = "stock_inicio", nullable = false)
    @Builder.Default
    private Integer stockInicio = 0;

    @Column(name = "total_ingresos", nullable = false)
    @Builder.Default
    private Integer totalIngresos = 0;

    @Column(name = "total_salidas", nullable = false)
    @Builder.Default
    private Integer totalSalidas = 0;

    /**
     * GENERATED ALWAYS AS (stock_inicio + total_ingresos - total_salidas) STORED en MySQL.
     * JPA no lo escribe. MySQL lo calcula automáticamente al INSERT/UPDATE.
     */
    @Column(name = "stock_fin", insertable = false, updatable = false)
    private Integer stockFin;

    @Column(name = "total_liquidado", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalLiquidado = BigDecimal.ZERO;

    /**
     * TRUE = periodo contable bloqueado.
     * Solo ADMIN puede reabrir un kardex cerrado.
     * El contador solo puede auditar kardex cerrados — datos congelados.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean cerrado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generado_por_id")
    private Usuario generadoPor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
