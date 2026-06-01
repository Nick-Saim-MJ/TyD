package com.tyd.kitprepago.modulo_ventas.repository;

import com.tyd.kitprepago.modulo_inventario.entity.ItemKit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface ItemKitLockRepository extends JpaRepository<ItemKit, Long> {
    /**
     * SELECT ... FOR UPDATE: bloquea la fila hasta que la transacción termine.
     * Garantiza que dos vendedores simultáneos no vendan el mismo kit.
     * DEBE llamarse dentro de @Transactional.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ik FROM ItemKit ik WHERE ik.id = :id")
    Optional<ItemKit> findByIdForUpdate(@Param("id") Long id);
}
