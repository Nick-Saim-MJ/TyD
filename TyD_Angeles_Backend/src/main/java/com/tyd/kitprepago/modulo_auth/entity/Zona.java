package com.tyd.kitprepago.modulo_auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "zonas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Zona {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_zona", nullable = false, unique = true, length = 20)
    private String codigoZona;           // Z122000, Z122002, etc.

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String region;

    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "zona", fetch = FetchType.LAZY)
    private List<Sucursal> sucursales;
}