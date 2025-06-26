package org.example.yukiacademy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor; // Necesario para el constructor sin argumentos de Lombok
import lombok.AllArgsConstructor; // Necesario para el constructor con todos los argumentos de Lombok

@Data // Genera getters, setters, toString, equals, y hashCode
@NoArgsConstructor // Genera un constructor sin argumentos
@AllArgsConstructor // Genera un constructor con todos los argumentos
public class RegisterRequest {

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser una dirección de correo válida")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, max = 40, message = "La contraseña debe tener entre 6 y 40 caracteres")
    private String password;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres")
    private String lastName;

    // Opcional: si quieres permitir al usuario elegir el rol en el registro (con validación de seguridad para evitar roles no permitidos)
    // private String role; // Ejemplo: "student" o "professor"
}