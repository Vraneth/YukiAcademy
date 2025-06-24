package org.example.yukiacademy.config;

import org.example.yukiacademy.security.handler.AuthEntryPointJwt;
import org.example.yukiacademy.security.jwt.AuthTokenFilter;
import org.example.yukiacademy.security.jwt.JwtUtils;
import org.example.yukiacademy.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.cors.CorsConfiguration; // Importa CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Importa UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter; // Importa CorsFilter
import java.util.Arrays; // Importa Arrays

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
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        // Añade el filtro CORS ANTES del filtro de autenticación de Spring Security
        // Es CRÍTICO que el filtro CORS se ejecute muy temprano en la cadena de filtros.
        http.addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter.class);

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

    // Definición del bean CorsFilter
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Permite credenciales (cookies, encabezados de autenticación)

        // **AQUÍ ESTÁ EL CAMBIO CLAVE:**
        // Asegúrate de que el origen de tu frontend (http://localhost:5174) esté permitido.
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:5175", "http://localhost:3000")); // Puedes añadir más orígenes si es necesario

        config.addAllowedHeader("*"); // Permite todos los encabezados
        config.addAllowedMethod("*"); // Permite todos los métodos HTTP (GET, POST, PUT, DELETE, etc.)
        source.registerCorsConfiguration("/**", config); // Aplica esta configuración a todas las rutas
        return new CorsFilter(source);
    }
}