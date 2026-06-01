package com.tyd.kitprepago.shared.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyd.kitprepago.shared.zona.ZonaContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspecto de auditoría. Intercepta métodos anotados con @Auditable
 * y persiste el registro en audit_log.
 *
 * Se usa @AfterReturning porque solo auditamos operaciones EXITOSAS.
 * Si el método lanza excepción (rollback), no debe existir registro de auditoría.
 *
 * Importante: este aspecto opera FUERA de la transacción del servicio auditado
 * para que el registro de auditoría NO se revierta si algo falla después.
 * Usar @Transactional(propagation = REQUIRES_NEW) en el método de persistencia.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final ZonaContextHolder zonaContextHolder;
    private final ObjectMapper objectMapper;

    /**
     * Intercepta cualquier método anotado con @Auditable en cualquier clase
     * dentro del paquete de features.
     *
     * @param joinPoint     Punto de unión (método interceptado)
     * @param auditable     La anotación @Auditable del método
     * @param resultado     El objeto retornado por el método (puede ser DTO o entidad)
     */



    @AfterReturning(pointcut = "@annotation(auditable)", returning = "resultado")
    public void registrarAuditoria(JoinPoint joinPoint, Auditable auditable, Object resultado) {
        // SI ESTO NO SALE EN CONSOLA, EL PROBLEMA ES EL PROXY DE SPRING
        log.info("🔥 PROCESANDO AUDITORIA: {} en tabla {}",
                joinPoint.getSignature().getName(), auditable.tabla());

        try {
            Long usuarioId = zonaContextHolder.getUsuarioActualId();
            String ip = extraerIp();

            // Extracción de ID corregida para Records y Clases
            Long registroId = extraerIdSeguro(resultado);
            String datosNuevosJson = serializarSeguro(resultado);

            AuditLog entry = AuditLog.builder()
                    .tablaNombre(auditable.tabla())
                    .registroId(registroId)
                    .accion(auditable.accion())
                    .datosNuevos(datosNuevosJson)
                    .usuarioId(usuarioId)
                    .ipAddress(ip)
                    .build();

            auditLogRepository.guardarEnTransaccionIndependiente(entry);

        } catch (Exception ex) {
            log.error("❌ Error crítico en auditoría: {}", ex.getMessage());
        }
    }

    private Long extraerIdSeguro(Object obj) {
        if (obj == null) return -1L;
        try {
            // 1. Intentar como Record (método id())
            try {
                Object val = obj.getClass().getMethod("id").invoke(obj);
                if (val instanceof Long l) return l;
                if (val instanceof Integer i) return i.longValue();
            } catch (NoSuchMethodException ignored) {}

            // 2. Intentar como Clase (método getId())
            try {
                Object val = obj.getClass().getMethod("getId").invoke(obj);
                if (val instanceof Long l) return l;
                if (val instanceof Integer i) return i.longValue();
            } catch (NoSuchMethodException ignored) {}

        } catch (Exception e) {
            log.warn("No se pudo extraer ID: {}", e.getMessage());
        }
        return -1L;
    }

    private String extraerIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            // Considerar proxies/load balancers (Nginx delante de Spring Boot)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception ex) {
            return null;
        }
    }

    private String serializarSeguro(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception ex) {
            log.warn("No se pudo serializar objeto para auditoría: {}", ex.getMessage());
            return "{\"error\":\"serialization_failed\"}";
        }
    }
}
