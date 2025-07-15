package org.example.yukiacademy.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsRequest {
    @NotNull(message = "El estado de notificación de email no puede ser nulo.")
    private Boolean receiveEmailNotifications;
    @NotNull(message = "El estado de visibilidad del perfil no puede ser nulo.")
    private Boolean profileVisibleToPublic;
}