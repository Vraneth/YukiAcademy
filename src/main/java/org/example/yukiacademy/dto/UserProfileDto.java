// src/main/java/org/example/yukiacademy/dto/UserProfileDto.java

package org.example.yukiacademy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set; // <-- ¡IMPORTAR Set!

@Data
public class UserProfileDto {
    private Long id;
    private String email;
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String firstName;
    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres")
    private String lastName;
    // Removido profilePictureUrl y bio, ya que no son parte de este DTO específico de roles,
    // pero asegúrate de mantenerlos si tu UserProfileDto original los tenía y los necesitas para otras vistas.
    // Voy a mantenerlos tal como los tenías previamente por si acaso.
    private String profilePictureUrl; // Mantener si lo usas en el perfil
    @Size(max = 500, message = "La biografía no puede exceder los 500 caracteres")
    private String bio;
    @Size(max = 255, message = "Los intereses no pueden exceder los 255 caracteres")
    private String interests;

    private Set<String> roles; // <-- ¡NUEVO CAMPO PARA LOS ROLES!
}