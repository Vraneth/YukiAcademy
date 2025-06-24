package org.example.yukiacademy; // Puedes ponerlo en tu paquete principal o en uno temporal

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class JwtSecretGenerator {

    public static void main(String[] args) {
        // Genera una clave segura de 256 bits (32 bytes)
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512); // HS512 para mayor seguridad
        String secretString = Encoders.BASE64.encode(key.getEncoded());

        System.out.println("Tu clave secreta JWT generada y codificada en Base64 es:");
        System.out.println(secretString);
        System.out.println("\nCopia esta cadena y pégala en tu application.properties para 'yukiacademy.app.jwtSecret'");
        System.out.println("¡No la compartas ni la dejes en tu código fuente después de usarla!");
    }
}