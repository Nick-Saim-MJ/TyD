package com.tyd.kitprepago.shared.security;

/**
 * Roles del sistema. Orden de permiso de mayor a menor:
 * ADMIN > JEFE_ALMACEN > ALMACENERO = VENDEDOR > CONTADOR
 *
 * Notas de negocio:
 *  - CONTADOR: solo lectura de reportes, nunca escribe datos
 *  - JEFE_ALMACEN: ve todas las zonas de su región asignada (Percy López)
 *  - ALMACENERO: ve solo su zona, puede hacer ventas y despachos
 *  - VENDEDOR: ve solo su sucursal, puede registrar ventas
 */
public enum Rol {
    ADMIN,
    JEFE_ALMACEN,
    ALMACENERO,
    VENDEDOR,
    CONTADOR
}
