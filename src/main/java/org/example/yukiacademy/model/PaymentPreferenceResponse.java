package org.example.yukiacademy.model; // Asegúrate de que este paquete coincida con la ubicación real

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase DTO (Data Transfer Object) para encapsular la respuesta de Mercado Pago
 * al crear una preferencia de pago. Contiene el ID de la preferencia
 * y la URL de inicio del proceso de pago.
 */
@Data // Anotación de Lombok para generar getters, setters, toString, equals y hashCode
@NoArgsConstructor // Anotación de Lombok para generar un constructor sin argumentos
@AllArgsConstructor // Anotación de Lombok para generar un constructor con todos los argumentos
public class PaymentPreferenceResponse {
    private String preferenceId; // El ID único de la preferencia de pago generada por Mercado Pago
    private String initPoint;    // La URL inicial (link de pago) donde el usuario debe ser redirigido o usar el widget
}
