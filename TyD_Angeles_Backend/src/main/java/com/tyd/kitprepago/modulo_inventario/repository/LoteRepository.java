package com.tyd.kitprepago.modulo_inventario.repository;

import com.tyd.kitprepago.modulo_inventario.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long>,
        JpaSpecificationExecutor<Lote> {

    /**
     * Para el filtro del almacenero: busca por boucher exacto
     */
    Optional<Lote> findByNumeroOperacion(String numeroOperacion);

    /**
     * Para verificar duplicados al crear
     */
    boolean existsByNumeroPedido(String numeroPedido);

    boolean existsByNumeroOperacion(String numeroOperacion);

    /**
     * Lotes de una zona en un periodo — para el export Excel
     */
    @Query("""

            SELECT l FROM Lote l
        WHERE (:zonaId IS NULL OR l.zona.id = :zonaId)
          AND (:desde IS NULL OR l.fechaRecepcion >= :desde)
          AND (:hasta IS NULL OR l.fechaRecepcion <= :hasta)
        ORDER BY l.createdAt DESC
        """)
    List<Lote> findConFiltros(
            @Param("zonaId")  Long zonaId,
            @Param("desde") LocalDate desde,
            @Param("hasta")   LocalDate hasta
    );
}
