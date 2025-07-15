package org.example.yukiacademy.model; // Asegúrate de que este paquete coincida con la ubicación real

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPreferenceResponse {
    private String preferenceId;
    private String initPoint;
}
