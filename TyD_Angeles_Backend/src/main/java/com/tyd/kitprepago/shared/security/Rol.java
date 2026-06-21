package com.tyd.kitprepago.shared.security;

/**
 * Roles del sistema.
 *
 * Notas de negocio:
 *  - ADMIN: administrador técnico del sistema (usuarios, zonas, sucursales, pedidos).
 *           No está atado a una zona/sucursal propia.
 *  - CONTADOR: audita, gestiona y controla toda la empresa. Acceso equivalente a ADMIN
 *              en lectura y escritura sobre todos los módulos (ventas, despachos,
 *              usuarios, zonas, sucursales, kardex, liquidaciones, auditoría).
 *  - JEFE_ALMACEN: ve todas las sucursales de su región asignada. Hay varios,
 *                  uno por región (Puno, Cusco, MDD, Apurímac) — ninguno tiene
 *                  visión nacional.
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
