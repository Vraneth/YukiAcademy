package org.example.yukiacademy.dto.mercadopago;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponseDto {
    private String preferenceId;
    private String initPoint;
}