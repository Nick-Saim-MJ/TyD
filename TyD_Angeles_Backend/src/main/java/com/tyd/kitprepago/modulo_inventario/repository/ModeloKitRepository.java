package com.tyd.kitprepago.modulo_inventario.repository;

import com.tyd.kitprepago.modulo_inventario.entity.ModeloKit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModeloKitRepository extends JpaRepository<ModeloKit, Long> {
    List<ModeloKit> findByActivoTrue();

    boolean existsByCodigo(String codigo);
}
