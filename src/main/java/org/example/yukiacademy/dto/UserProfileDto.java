package org.example.yukiacademy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileDto {
    private Long id;
    private String email; // No se edita directamente desde el perfil
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String firstName;
    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres")
    private String lastName;
    private String profilePictureUrl;
    @Size(max = 500, message = "La biografía no puede exceder los 500 caracteres")
    private String bio;
    @Size(max = 255, message = "Los intereses no pueden exceder los 255 caracteres")
    private String interests;
}