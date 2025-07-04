// src/main/java/org/example/yukiacademy/dto/user/PrivacySettingsResponse.java

package org.example.yukiacademy.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para la respuesta de la configuración de privacidad de un usuario.
 * Refleja el estado actual de las preferencias.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsResponse {
    private Boolean receiveEmailNotifications;
    private Boolean profileVisibleToPublic;
    // Debe coincidir con los campos de PrivacySettingsRequest
}
