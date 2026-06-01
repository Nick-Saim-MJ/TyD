package com.tyd.kitprepago.modulo_ventas.repository;

import com.tyd.kitprepago.modulo_ventas.entity.Cliente;
import com.tyd.kitprepago.modulo_ventas.entity.TipoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDni(String dni);

    boolean existsByDni(String dni);

    boolean existsByRuc(String ruc);

    /**
     * Autocomplete: busca por DNI exacto, nombre parcial, apellido parcial o RUC exacto.
     * Límite de 10 resultados para no sobrecargar el dropdown del formulario.
     */
    @Query("""

            SELECT c FROM Cliente c
        WHERE LOWER(c.dni) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(c.nombres) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(COALESCE(c.ruc, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY c.apellidos, c.nombres
        LIMIT 10
        """)
    List<Cliente> buscarParaAutocomplete(@Param("q") String q);

    /**
     * Lista completa con filtro por tipo (para ADMIN y CONTADOR)
     */
    List<Cliente> findByTipoOrderByApellidosAscNombresAsc(TipoCliente tipo);
}
