// src/main/java/org/example/yukiacademy/dto/user/PrivacySettingsRequest.java

package org.example.yukiacademy.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para la solicitud de actualización de la configuración de privacidad de un usuario.
 * Define campos booleanos para diversas preferencias.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsRequest {
    private Boolean receiveEmailNotifications;
    private Boolean profileVisibleToPublic;
    // Añade más preferencias de privacidad si las tienes en tu modelo de usuario
}
