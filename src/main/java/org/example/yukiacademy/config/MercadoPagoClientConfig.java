// src/main/java/org/example/yukiacademy/config/MercadoPagoClientConfig.java

package org.example.yukiacademy.config;

import com.mercadopago.MercadoPagoConfig; // Importación necesaria para la clase estática del SDK
import com.mercadopago.client.preference.PreferenceClient;
import org.springframework.beans.factory.annotation.Value; // Necesario para @Value
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Indica que esta clase es una fuente de definiciones de beans.
public class MercadoPagoClientConfig { // Clase renombrada para evitar conflictos

    // Eliminamos la inyección del campo de instancia, ya que inyectaremos el valor directamente en el método @Bean.
    // @Value("${mercadopago.access_token}")
    // private String mercadoPagoAccessToken;


    public MercadoPagoClientConfig() {
        // El constructor sigue sin necesidad de lógica específica aquí.
    }

    // Este método crea un bean de PreferenceClient que Spring puede inyectar.
    // ¡Ahora inyectamos el accessToken directamente como un parámetro del método!
    @Bean
    public PreferenceClient preferenceClient(
            @Value("${mercadopago.access_token}") String mercadoPagoAccessToken) { // <-- ¡CORRECCIÓN CLAVE AQUÍ!
    
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken); // Corrección aquí
        return new PreferenceClient();
    }
}