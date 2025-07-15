package org.example.yukiacademy.dto.mercadopago;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckoutRequestDto {

    @NotEmpty(message = "La lista de ítems del carrito no puede estar vacía")
    @Valid
    private List<CartItemDto> items;
}