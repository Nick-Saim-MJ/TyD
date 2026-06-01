package com.tyd.kitprepago.modulo_auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "sucursales")
@SQLRestriction("deleted_at IS NULL")    // Hibernate 6.x: reemplaza @Where
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sucursal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoSucursal tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(nullable = false, length = 10)
    private String ubigeo;

    @Column(nullable = false, length = 255)
    private String direccion;

    /**
     * Self-referencia: dos sucursales que comparten ubicación física.
     * Ej: Almacén Juliaca y Oficina 2 Juliaca tienen la misma dirección.
     * El módulo de inventario usa este campo para mostrar kits disponibles
     * del almacén como disponibles también en la oficina vinculada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_fisica_id")
    private Sucursal ubicacionFisica;

    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;           // Soft delete — nunca DELETE físico

    public enum TipoSucursal { ALMACEN, OFICINA }
}
