package com.tyd.kitprepago.modulo_inventario.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "productos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String nombre;
    @Column(length = 500)
    private String descripcion;
    @Column(name = "precio_regular", precision = 10, scale = 2)
    private BigDecimal precioRegular;
    @Builder.Default private Boolean activo = true;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
