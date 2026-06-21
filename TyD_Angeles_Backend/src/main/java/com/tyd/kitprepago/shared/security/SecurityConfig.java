package com.tyd.kitprepago.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de Spring Security.
 *
 * @EnableMethodSecurity habilita @PreAuthorize en controllers,
 * lo que permite control de acceso a nivel de método además del filtro URL.
 * Ejemplo: @PreAuthorize("hasRole('ADMIN') or hasRole('JEFE_ALMACEN')")
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF: usamos JWT stateless, no sesiones ni cookies de sesión
                .csrf(AbstractHttpConfigurer::disable)

                // CORS: Angular en localhost:4200 (dev) o dominio de producción
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Sin sesión HTTP: cada request es autónomo con su JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Endpoint de login: público
                        .requestMatchers("/api/auth/login").permitAll()

                        // Swagger/OpenAPI: solo en dev (en prod configurar para que requiera auth)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ── Módulo A: Org & Accesos ──────────────────────────
                        // ADMIN y CONTADOR gestionan usuarios y sucursales
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyRole("ADMIN","CONTADOR")
                        .requestMatchers(HttpMethod.PUT,  "/api/usuarios/**").hasAnyRole("ADMIN","CONTADOR")
                        .requestMatchers(HttpMethod.DELETE,"/api/usuarios/**").hasAnyRole("ADMIN","CONTADOR")
                        // Cualquier autenticado puede ver lista de vendedores (para autocomplete)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/vendedores").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasAnyRole("ADMIN","CONTADOR")

                        // ── Módulo B: Inventario ──────────────────────────────
                        // ADMIN, JEFE_ALMACEN, ALMACENERO y CONTADOR crean lotes; el resto solo lee
                        .requestMatchers(HttpMethod.POST, "/api/lotes/**")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","ALMACENERO","CONTADOR")
                        .requestMatchers(HttpMethod.GET,  "/api/lotes/**").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/items-kit/**").authenticated()
                        // ADMIN, JEFE_ALMACEN y CONTADOR pueden cambiar estado de un kit
                        .requestMatchers(HttpMethod.PATCH, "/api/items-kit/*/estado")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","CONTADOR")

                        // ── Módulo C: Logística ───────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/despachos", "/api/despachos/**")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","ALMACENERO","CONTADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/despachos/*/confirmar-recepcion")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","ALMACENERO","CONTADOR")
                        .requestMatchers(HttpMethod.GET, "/api/despachos/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/mis-recepciones").authenticated()

                        // ── Módulo D: Ventas ──────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/ventas")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","ALMACENERO","VENDEDOR","CONTADOR")
                        .requestMatchers(HttpMethod.POST, "/api/ventas/*/anular")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","CONTADOR")
                        .requestMatchers(HttpMethod.GET, "/api/ventas/**").authenticated()
                        .requestMatchers("/api/clientes/**").authenticated()
                        .requestMatchers("/api/liquidaciones/**").authenticated()
                        .requestMatchers("/api/activaciones/**").authenticated()
                        .requestMatchers("/api/modelos-kit/**").authenticated()

                        // ── Módulo E: Reportes ────────────────────────────────
                        .requestMatchers("/api/reportes/**")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","CONTADOR")
                        .requestMatchers(HttpMethod.POST, "/api/kardex/**")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","CONTADOR")
                        .requestMatchers(HttpMethod.GET, "/api/kardex/**")
                        .hasAnyRole("ADMIN","JEFE_ALMACEN","CONTADOR")
                        .requestMatchers("/api/audit/**").hasAnyRole("ADMIN","CONTADOR")

                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )

                // Nuestro filtro JWT va ANTES del filtro estándar de usuario/contraseña
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Provider que usa BCrypt para verificar contraseñas
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt con strength 12 (recomendado para producción)
        // Strength 10 = ~100ms/hash, 12 = ~400ms/hash en hardware moderno
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // En producción, reemplazar con el dominio real del frontend Angular
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",          // Angular dev server
                "https://kitprepago.tyd.pe"       // Producción
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        config.setExposedHeaders(List.of("Content-Disposition")); // Para descarga de Excel
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}