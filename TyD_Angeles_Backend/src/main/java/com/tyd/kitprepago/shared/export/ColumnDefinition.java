package com.tyd.kitprepago.shared.export;

import lombok.Getter;
import java.util.function.Function;

/**
 * Define una columna del Excel: nombre del header y cómo extraer el valor del objeto.
 *
 * Uso:
 *   new ColumnDefinition<>("Serie SIM", VentaDto::getSerieSim)
 *   new ColumnDefinition<>("Monto",     VentaDto::getMonto)
 */
@Getter
public class ColumnDefinition<T> {

    private final String nombre;
    private final Function<T, Object> extractor;

    public ColumnDefinition(String nombre, Function<T, Object> extractor) {
        this.nombre    = nombre;
        this.extractor = extractor;
    }
}
