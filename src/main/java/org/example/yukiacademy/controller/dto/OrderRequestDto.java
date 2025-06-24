package org.example.yukiacademy.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {
    @NotEmpty(message = "Debe haber al menos un curso en la orden")
    private List<Long> courseIds; // IDs de los cursos a comprar
}