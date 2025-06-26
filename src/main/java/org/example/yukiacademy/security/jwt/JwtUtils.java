package org.example.yukiacademy.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm; // No necesario con builder moderno, pero se mantiene si se usaba así.
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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

    // ¡CAMBIO CLAVE AQUÍ! Usamos ${jwt.secret} para que coincida con application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    // ¡CAMBIO CLAVE AQUÍ! Usamos ${jwt.expiration} para que coincida con application.properties
    @Value("${jwt.expiration}")
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
                .signWith(key(), SignatureAlgorithm.HS256) // Se mantiene HS256
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
        } catch (Exception e) { // Captura cualquier otra excepción inesperada
            logger.error("Error validando el token JWT: {}", e.getMessage());
        }
        return false;
    }
}