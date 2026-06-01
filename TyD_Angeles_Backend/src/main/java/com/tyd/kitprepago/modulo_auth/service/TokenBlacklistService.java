package com.tyd.kitprepago.modulo_auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blacklist de tokens JWT para el logout.
 *
 * Implementación en memoria con ConcurrentHashMap.
 * Es suficiente para un monolito con un solo nodo.
 *
 * Si en el futuro se escala a múltiples instancias, reemplazar
 * con Redis: redisTemplate.opsForValue().set(token, "1", ttl)
 *
 * La limpieza automática ocurre cada hora vía @Scheduled.
 * Un token expirado en JWT ya no puede usarse aunque no esté en la blacklist,
 * así que el cleanup es solo para liberar memoria, no es crítico.
 *
 * Activar en la clase principal o en una @Configuration:
 *   @EnableScheduling
 */
@Service
@Slf4j
public class TokenBlacklistService {

    // token → expiración (Instant). Cuando expira, se puede limpiar.
    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    /**
     * Invalida un token. Llamado desde AuthService.logout().
     * @param token     El JWT completo
     * @param expiracion Cuándo vence el token (para poder limpiarlo después)
     */
    public void invalidar(String token, Instant expiracion) {
        blacklist.put(token, expiracion);
        log.debug("Token invalidado. Blacklist size: {}", blacklist.size());
    }

    /**
     * Verifica si un token está en la blacklist.
     * Llamado desde JwtAuthenticationFilter antes de autenticar.
     */
    public boolean estaInvalidado(String token) {
        return blacklist.containsKey(token);
    }

    /**
     * Limpieza automática cada hora.
     * Elimina tokens que ya expiraron — no tienen valor en la blacklist
     * porque JWT ya los rechazaría por fecha de expiración.
     */
    @Scheduled(fixedRate = 3_600_000)  // cada 1 hora en ms
    public void limpiarTokensExpirados() {
        Instant ahora = Instant.now();
        int antes = blacklist.size();
        blacklist.entrySet().removeIf(e -> e.getValue().isBefore(ahora));
        int eliminados = antes - blacklist.size();
        if (eliminados > 0) {
            log.info("Blacklist cleanup: {} tokens expirados eliminados. Restantes: {}",
                    eliminados, blacklist.size());
        }
    }
}