package com.tyd.kitprepago.modulo_ventas.entity;

import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Activación del servicio DirecTV asociada a una venta.
 *
 * Se crea automáticamente en estado PENDIENTE al registrar una venta.
 * Se confirma a ACTIVO cuando DirecTV procesa la activación.
 *
 * La activación diferida es el caso normal del flujo: la venta se registra
 * inmediatamente, pero la activación puede confirmarse después,
 * una vez que el cliente recarga y DirecTV procesa la señal.
 *
 * fecha_activacion = NULL mientras estado = PENDIENTE.
 * registrado_por_id puede ser diferente al vendedor original
 * (quien confirma la activación puede ser el almacenero o jefe).
 */
@Entity
@Table(name = "activaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Activacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    private Venta venta;

    @Column(name = "monto_recarga_inicial", precision = 10, scale = 2)
    private BigDecimal montoRecargaInicial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private EstadoActivacion estado = EstadoActivacion.PENDIENTE;

    /** NULL mientras PENDIENTE */
    @Column(name = "fecha_activacion")
    private Instant fechaActivacion;

    /** Quien confirmó la activación en el sistema */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    @Column(length = 500)
    private String comentarios;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
