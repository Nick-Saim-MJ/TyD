package com.tyd.kitprepago.modulo_ventas.repository;

import com.tyd.kitprepago.modulo_ventas.entity.Activacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivacionRepository extends JpaRepository<Activacion, Long> {

    Optional<Activacion> findByVentaId(Long ventaId);

    /**
     * Activaciones pendientes con detalle completo.
     * Filtradas por zona si el usuario no es ADMIN/CONTADOR.
     */
    @Query("""

            SELECT a FROM Activacion a
        JOIN FETCH a.venta v
        JOIN FETCH v.itemKit ik
        JOIN FETCH ik.producto
        JOIN FETCH v.cliente
        JOIN FETCH v.sucursalVenta sv
        JOIN FETCH sv.zona z
        WHERE a.estado = com.tyd.kitprepago.modulo_ventas.entity.EstadoActivacion.PENDIENTE
          AND (:zonaId IS NULL OR z.id = :zonaId)
        ORDER BY v.fechaVenta ASC
        """)
    List<Activacion> findPendientesPorZona(@Param("zonaId") Long zonaId);
}
