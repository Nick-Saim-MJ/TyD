package com.tyd.kitprepago.modulo_inventario.entity;

import com.tyd.kitprepago.modulo_auth.entity.Sucursal;
import com.tyd.kitprepago.modulo_auth.entity.Usuario;
import com.tyd.kitprepago.modulo_auth.entity.Zona;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Agrupa los kits de un mismo cargamento DirecTV.
 *
 * DISTINCIÓN IMPORTANTE:
 *  - numero_pedido    = N° de seguimiento del cargamento (lo usa Logística)
 *  - numero_operacion = N° boucher de compra TyD→DirecTV (lo usa Admin/Contador)
 *
 * Un lote pertenece a UNA zona pero los kits pueden redistribuirse
 * a distintas sucursales de esa zona (y de otras) vía despachos.
 */
@Entity
@Table(name = "lotes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lote {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", unique = true, length = 50)
    private String numeroPedido;        // Logística: N° seguimiento cargamento

    @Column(name = "numero_operacion", length = 50)
    private String numeroOperacion;     // Administración/Contador: boucher de compra

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_recepcion_id", nullable = false)
    private Sucursal sucursalRecepcion;

    @Column(name = "cantidad_esperada", nullable = false)
    private Integer cantidadEsperada;

    @Column(name = "cantidad_recibida")
    @Builder.Default
    private Integer cantidadRecibida = 0;

    @Column(name = "fecha_pedido")
    private LocalDate fechaPedido;

    @Column(name = "fecha_recepcion")
    private LocalDate fechaRecepcion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id", nullable = false)
    private Usuario usuarioRegistro;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY)
    private List<ItemKit> items;
}
