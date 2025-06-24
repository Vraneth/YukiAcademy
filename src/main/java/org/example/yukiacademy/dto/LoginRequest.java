package org.example.yukiacademy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok
public class LoginRequest {

    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}