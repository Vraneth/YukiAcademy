package org.example.yukiacademy.dto.mercadopago;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponseDto {
    private String preferenceId; // El ID de la preferencia creada en Mercado Pago
    private String initPoint;    // La URL de pago a la que el frontend debe redirigir al usuario
}