package com.tyd.kitprepago.modulo_inventario.entity;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Unidad mínima de inventario: un kit físico serializado.
 *
 * Reglas de negocio codificadas en la entidad:
 *  - serie_deco puede ser NULL (kits sin decodificador, ej: solo SIM)
 *  - serie_maestro y serie_sim son siempre obligatorios
 *  - estado controla qué operaciones son válidas sobre el kit
 *
 * Índices importantes (definidos en BD):
 *  - idx_serie_maestro, idx_serie_sim, idx_serie_deco → búsqueda por escaneo
 *  - idx_estado_sucursal (estado, sucursal_actual_id) → stock disponible
 *
 * El "stock disponible" de una sucursal se calcula siempre con:
 *   COUNT(*) WHERE estado='DISPONIBLE' AND sucursal_actual_id=X
 * NUNCA usar kardex_mensual para datos en tiempo real.
 */
@Entity
@Table(name = "items_kit",
    indexes = {
        @Index(name = "idx_serie_maestro",   columnList = "serie_maestro"),
        @Index(name = "idx_serie_sim",        columnList = "serie_sim"),
        @Index(name = "idx_serie_deco",       columnList = "serie_deco"),
        @Index(name = "idx_estado_sucursal",  columnList = "estado, sucursal_actual_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemKit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_kit_id")
    private ModeloKit modeloKit;

    @Column(name = "serie_maestro", nullable = false, unique = true, length = 100)
    private String serieMaestro;

    @Column(name = "serie_sim", nullable = false, unique = true, length = 50)
    private String serieSim;

    /**
     * NULL permitido — kits sin decodificador (ModeloKit.tieneDeco = false).
     * Si tiene valor, debe ser único a nivel global (UNIQUE constraint en BD).
     */
    @Column(name = "serie_deco", unique = true, length = 100)
    private String serieDeco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoKit estado = EstadoKit.DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_actual_id")
    private Sucursal sucursalActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodio_actual_id")
    private Usuario custodioActual;

    @CreationTimestamp
    @Column(name = "fecha_ingreso", updatable = false)
    private Instant fechaIngreso;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Helpers de negocio ───────────────────────────────────────────────────

    public boolean isDisponible() {
        return EstadoKit.DISPONIBLE.equals(this.estado);
    }

    public boolean isVendido() {
        return EstadoKit.VENDIDO.equals(this.estado);
    }

    /**
     * Marca el kit como vendido. Llamado dentro de @Transactional en VentaService.
     * No limpiar custodioActual aquí — el historial_custodios lo registra.
     */
    public void marcarVendido() {
        this.estado = EstadoKit.VENDIDO;
    }

    public void marcarEnTransito() {
        this.estado = EstadoKit.TRANSITO;
    }

    public void marcarDisponibleEn(Sucursal destino, Usuario custodio) {
        this.estado = EstadoKit.DISPONIBLE;
        this.sucursalActual = destino;
        this.custodioActual = custodio;
    }
}
