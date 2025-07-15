package org.example.yukiacademy.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsResponse {
    private Boolean receiveEmailNotifications;
    private Boolean profileVisibleToPublic;
}