package org.example.yukiacademy.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para la solicitud de cambio de contraseña de un usuario.
 * Contiene la contraseña actual y la nueva contraseña (con confirmación).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria.")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 6, max = 40, message = "La nueva contraseña debe tener entre 6 y 40 caracteres.")
    private String newPassword;

    @NotBlank(message = "La confirmación de la nueva contraseña es obligatoria.")
    private String confirmNewPassword;
}