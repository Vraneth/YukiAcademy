package org.example.yukiacademy.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
// Añadimos estas importaciones explícitas para ayudar al IDE a resolver los tipos
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Claims; // Aunque no se usa directamente en este método, es una buena práctica si manejas claims

import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${yuki.academy.jwtSecret}") // Se obtiene del application.properties
    private String jwtSecret;

    @Value("${yuki.academy.jwtExpirationMs}") // Se obtiene del application.properties
    private int jwtExpirationMs;

public String generateJwtToken(Authentication authentication) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
    
    return Jwts.builder()
            .claims()
            .subject(userPrincipal.getUsername())
            .add("id", userPrincipal.getId())
            .add("firstName", userPrincipal.getFirstName())
            .add("lastName", userPrincipal.getLastName())
            .issuedAt(new Date())
            .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .and()
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
}

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

public String getEmailFromJwtToken(String token) {
    return Jwts.parser()
            .setSigningKey(key())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
}

public boolean validateJwtToken(String authToken) {
    try {
        Jwts.parser()
            .setSigningKey(key())
            .build()
            .parseSignedClaims(authToken);
        return true;
    } catch (MalformedJwtException e) {
        logger.error("Token JWT inválido: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
        logger.error("Token JWT expirado: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
        logger.error("Token JWT no soportado: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
        logger.error("La cadena de claims JWT está vacía: {}", e.getMessage());
    } catch (Exception e) {
        logger.error("Error validando el token JWT: {}", e.getMessage());
    }
    return false;
}
}