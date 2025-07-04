// src/main/java/org/example/yukiacademy/dto/mercadopago/CartItemDto.java
package org.example.yukiacademy.dto.mercadopago;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data // Anotación de Lombok para getters, setters, equals, hashCode y toString
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los argumentos
public class CartItemDto {

    @NotNull(message = "El ID del curso no puede ser nulo")
    private Long courseId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;
}