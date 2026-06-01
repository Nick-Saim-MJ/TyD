package com.tyd.kitprepago.shared.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio centralizado para todo lo relacionado con JWT.
 * Usa JJWT 0.12.x — la API cambió respecto a 0.11.x:
 *   - Jwts.builder() se mantiene igual
 *   - signWith(key) ya no necesita el algoritmo explícito
 *   - Jwts.parser() reemplaza a Jwts.parserBuilder()
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ─────────────────────── GENERACIÓN ───────────────────────

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        UsuarioPrincipal principal = (UsuarioPrincipal) userDetails;
        // Incluimos rol y zonaId en el token para que los guards
        // no tengan que hacer query a BD en cada request
        extraClaims.put("rol", principal.getRol().name());
        extraClaims.put("zonaId", principal.getZonaId());
        extraClaims.put("sucursalId", principal.getSucursalId());
        extraClaims.put("nombreCompleto", principal.getNombreCompleto());
        return buildToken(extraClaims, userDetails, expirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpirationMs);
    }

    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ─────────────────────── VALIDACIÓN ───────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ─────────────────────── EXTRACCIÓN ───────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Rol extractRol(String token) {
        String rolStr = extractClaim(token, claims -> claims.get("rol", String.class));
        return Rol.valueOf(rolStr);
    }

    public Long extractZonaId(String token) {
        return extractClaim(token, claims -> {
            Object v = claims.get("zonaId");
            if (v == null) return null;
            return v instanceof Integer ? ((Integer) v).longValue() : (Long) v;
        });
    }

    public Long extractSucursalId(String token) {
        return extractClaim(token, claims -> {
            Object v = claims.get("sucursalId");
            if (v == null) return null;
            return v instanceof Integer ? ((Integer) v).longValue() : (Long) v;
        });
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // JJWT 0.12.x: Jwts.parser() en lugar de Jwts.parserBuilder()
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
