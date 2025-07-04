// src/main/java/org/example/yukiacademy/dto/mercadopago/CartCheckoutRequestDto.java
package org.example.yukiacademy.dto.mercadopago;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List; // ¡¡¡IMPORTACIÓN AÑADIDA!!!

import jakarta.validation.constraints.NotEmpty; // Para validar que la lista no esté vacía
import jakarta.validation.Valid; // Para validar los objetos dentro de la lista

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckoutRequestDto {

    @NotEmpty(message = "La lista de ítems del carrito no puede estar vacía")
    @Valid // Esta anotación asegura que cada CartItemDto dentro de la lista también sea validado
    private List<CartItemDto> items;
}