package com.tyd.kitprepago.modulo_logistica.repository;

import com.tyd.kitprepago.modulo_logistica.entity.DespachoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DespachoItemRepository extends JpaRepository<DespachoItem, Long> {

    /** Verificar si un kit ya está en un despacho activo (EN_TRANSITO) */
    @Query("""
        SELECT COUNT(di) > 0 FROM DespachoItem di
        WHERE di.itemKit.id = :itemKitId
          AND di.despacho.estado = com.tyd.kitprepago.modulo_logistica.entity.EstadoDespacho.EN_TRANSITO
        """)
    boolean existeKitEnDespachoActivo(@Param("itemKitId") Long itemKitId);
}
