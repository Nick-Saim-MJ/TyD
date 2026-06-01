package com.tyd.kitprepago.modulo_logistica.repository;

import com.tyd.kitprepago.modulo_logistica.entity.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    /**
     * Despachos filtrados por zona del usuario.
     * ADMIN/CONTADOR → zonaId=null → todos los despachos.
     * ALMACENERO/JEFE → zonaId=X → despachos donde origen O destino pertenece a esa zona.
     */
    @Query("""
        SELECT d FROM Despacho d
        JOIN FETCH d.sucursalOrigen so
        JOIN FETCH d.sucursalDestino sd
        JOIN FETCH so.zona
        JOIN FETCH sd.zona
        WHERE (:zonaId IS NULL
               OR so.zona.id = :zonaId
               OR sd.zona.id = :zonaId)
        ORDER BY d.createdAt DESC
        """)
    List<Despacho> findConFiltroZona(@Param("zonaId") Long zonaId);

    /**
     * Despachos EN_TRANSITO pendientes de recepción en la sucursal destino del usuario.
     * El almacenero receptor entra aquí para confirmar lo que le llegó.
     */
    @Query("""
        SELECT d FROM Despacho d
        JOIN FETCH d.sucursalOrigen so
        JOIN FETCH d.sucursalDestino sd
        JOIN FETCH d.usuarioEnvia
        WHERE d.estado = com.tyd.kitprepago.modulo_logistica.entity.EstadoDespacho.EN_TRANSITO
          AND sd.id = :sucursalDestinoId
        ORDER BY d.fechaDespacho ASC
        """)
    List<Despacho> findPendientesRecepcionEnSucursal(@Param("sucursalDestinoId") Long sucursalDestinoId);

    /**
     * Detalle completo con todos los items — para GET /api/despachos/{id}.
     * Usa JOIN FETCH para evitar N+1 queries al acceder a los items.
     */
    @Query("""
        SELECT DISTINCT d FROM Despacho d
        LEFT JOIN FETCH d.items di
        LEFT JOIN FETCH di.itemKit ik
        LEFT JOIN FETCH ik.producto
        LEFT JOIN FETCH ik.sucursalActual
        JOIN FETCH d.sucursalOrigen so
        JOIN FETCH d.sucursalDestino sd
        JOIN FETCH so.zona
        JOIN FETCH sd.zona
        LEFT JOIN FETCH d.usuarioEnvia
        LEFT JOIN FETCH d.usuarioRecibe
        WHERE d.id = :id
        """)
    Optional<Despacho> findByIdConItems(@Param("id") Long id);
}
