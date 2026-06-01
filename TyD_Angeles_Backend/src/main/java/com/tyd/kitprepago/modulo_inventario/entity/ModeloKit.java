package com.tyd.kitprepago.modulo_inventario.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity @Table(name = "modelos_kit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModeloKit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(length = 500)
    private String descripcion;
    @Column(name = "tiene_deco", nullable = false)
    @Builder.Default private Boolean tieneDeco = true;
    @Builder.Default private Boolean activo = true;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
