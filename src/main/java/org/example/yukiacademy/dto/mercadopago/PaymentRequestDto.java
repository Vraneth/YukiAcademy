// src/main/java/org/example/yukiacademy/dto/mercadopago/PaymentRequestDto.java

package org.example.yukiacademy.dto.mercadopago;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// DTO para la solicitud de creación de preferencia de pago desde el frontend
@Data // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos
public class PaymentRequestDto {

    @NotNull(message = "El ID del curso no puede ser nulo.")
    private Long courseId; // El ID del curso que se va a comprar

    @NotNull(message = "La cantidad no puede ser nula.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1.")
    private Integer quantity; // La cantidad de este curso
}
