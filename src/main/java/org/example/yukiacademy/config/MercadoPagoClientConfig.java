package org.example.yukiacademy.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoClientConfig {

    public MercadoPagoClientConfig() {

    }

    @Bean
    public PreferenceClient preferenceClient(
            @Value("${mercadopago.access_token}") String mercadoPagoAccessToken) {
    
        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
        return new PreferenceClient();
    }
}