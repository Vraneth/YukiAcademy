package org.example.yukiacademy.config;

import org.example.yukiacademy.security.handler.AuthEntryPointJwt;
import org.example.yukiacademy.security.jwt.AuthTokenFilter;
import org.example.yukiacademy.security.jwt.JwtUtils;
import org.example.yukiacademy.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // ¡Importación necesaria!
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtUtils jwtUtils;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, AuthEntryPointJwt unauthorizedHandler, JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para APIs REST
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // Manejo de excepciones de autenticación
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Configura sesiones sin estado (para JWT)
                .authorizeHttpRequests(auth -> auth
                        // Permite acceso público a API de autenticación, Swagger UI, consola H2
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/h2-console/**"
                        ).permitAll() // Estas rutas son totalmente públicas (sin métodos específicos)

                        // ¡¡¡CAMBIO CLAVE AQUÍ: Separamos las reglas para los métodos GET!!!
                        // Permite todas las solicitudes GET a /api/courses y /api/courses/{id}
                        .requestMatchers(HttpMethod.GET, "/api/courses").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/{id}").permitAll()

                        // ¡¡¡CAMBIO CLAVE AQUÍ: Permite acceso público a recursos estáticos!!!
                        // Esto cubre archivos directamente en /static/ (ej: /curso1.webp)
                        .requestMatchers(
                                "/*.webp", "/*.jpg", "/*.png", "/*.ico" // Para imágenes directamente en /static/
                        ).permitAll()
                        // Esto cubre archivos dentro de subcarpetas de /static/ (ej: /images/curso1.webp, /css/, /js/)
                        .requestMatchers(
                                "/images/**", "/css/**", "/js/**", "/webjars/**"
                        ).permitAll()

                        // Permite la raíz y el index.html (útil si tu frontend es servido por el backend en producción)
                        .requestMatchers("/", "/index.html").permitAll()

                        // Cualquier otra solicitud requiere autenticación
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        // Añade tu filtro JWT antes del filtro de autenticación de usuario y contraseña
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        // Añade el filtro CORS antes del filtro de autenticación de usuario y contraseña
        http.addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter.class);

        // Permite que la consola H2 se muestre en un iframe (si la usas)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // Define los orígenes permitidos para CORS
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173", // Tu frontend de React/Vite
                "http://localhost:5174",
                "http://localhost:5175"
                // Aquí puedes añadir la URL de Ngrok también si tu frontend se desplegara a través de Ngrok
                // por ejemplo: "https://*.ngrok-free.app"
        ));

        // Permite todos los headers y métodos HTTP
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config); // Aplica esta configuración CORS a todas las rutas
        return new CorsFilter(source);
    }
}
