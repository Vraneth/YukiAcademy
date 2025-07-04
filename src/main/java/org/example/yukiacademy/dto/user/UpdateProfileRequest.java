// src/main/java/org/example/yukiacademy/dto/user/UpdateProfileRequest.java

package org.example.yukiacademy.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres.")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacío.")
    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres.")
    private String lastName;

    @Size(max = 500, message = "La biografía no puede exceder los 500 caracteres.")
    private String bio;

    @Size(max = 255, message = "La URL de la imagen de perfil no puede exceder los 255 caracteres.")
    private String profilePictureUrl;
}