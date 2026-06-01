package com.tyd.kitprepago.shared.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un método de servicio para que sea auditado automáticamente.
 * El AuditAspect intercepta la llamada y registra en audit_log.
 *
 * Uso:
 *   @Auditable(tabla = "ventas", accion = TipoAccion.INSERT)
 *   public VentaDto registrarVenta(RegistrarVentaRequest request) { ... }
 *
 * El aspecto captura el resultado del método (que debe retornar un objeto
 * con getId()) y lo persiste en audit_log con el snapshot JSON.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Nombre de la tabla afectada. Ej: "ventas", "items_kit", "usuarios"
     */
    String tabla();

    /**
     * Tipo de operación para el registro de auditoría.
     */
    TipoAccion accion();

    /**
     * Si es true, captura también el estado ANTES del cambio.
     * Solo aplica para UPDATE. Tiene costo adicional (query extra).
     */
    boolean capturarAnterior() default false;
}
