package com.tyd.kitprepago.modulo_inventario.repository;

import com.tyd.kitprepago.modulo_inventario.entity.HistorialCustodio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialCustodioRepository extends JpaRepository<HistorialCustodio, Long> {

    /**
     * Línea de vida completa de un kit, ordenada cronológicamente.
     * Para GET /api/items-kit/{id}/historial
     */
    @Query("""

            SELECT hc FROM HistorialCustodio hc
        LEFT JOIN FETCH hc.sucursalAnterior
        LEFT JOIN FETCH hc.sucursalNueva
        LEFT JOIN FETCH hc.custodioAnterior
        LEFT JOIN FETCH hc.custodioNuevo
        LEFT JOIN FETCH hc.registradoPor
        WHERE hc.itemKit.id = :itemKitId
        ORDER BY hc.fechaEvento ASC
        """)
    List<HistorialCustodio> findLineaDeVida(@Param("itemKitId") Long itemKitId);

    /**
     * Recepciones del almacenero autenticado.
     * Para GET /api/mis-recepciones
     */
    @Query("""
        SELECT hc FROM HistorialCustodio hc
        LEFT JOIN FETCH hc.itemKit ik
        LEFT JOIN FETCH ik.producto
        LEFT JOIN FETCH hc.sucursalNueva
        WHERE hc.registradoPor.id = :usuarioId
          AND hc.tipoEvento = com.tyd.kitprepago.modulo_inventario.entity.TipoEvento.INGRESO
        ORDER BY hc.fechaEvento DESC
        """)
    List<HistorialCustodio> findRecepcionesPorUsuario(@Param("usuarioId") Long usuarioId);
}
