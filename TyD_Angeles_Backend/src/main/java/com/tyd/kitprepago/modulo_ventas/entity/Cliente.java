package com.tyd.kitprepago.modulo_ventas.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Consumidor final del kit.
 *
 * tipo = PDV  → empresa/tienda (tiene RUC y razón social)
 * tipo = GENERAL → persona natural (solo DNI)
 *
 * El DNI es el identificador único para evitar duplicados.
 * Al registrar una venta, el formulario busca por DNI/nombre/RUC
 * y crea el cliente si no existe.
 */
@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(nullable = false, length = 255)
    private String nombres;

    @Column(nullable = false, length = 255)
    private String apellidos;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private TipoCliente tipo = TipoCliente.GENERAL;

    /** Solo para tipo PDV */
    @Column(name = "razon_social", length = 255)
    private String razonSocial;

    /** Solo para tipo PDV */
    @Column(length = 15, unique = true)
    private String ruc;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** Nombre completo para mostrar en listados */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
