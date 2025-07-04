// src/main/java/org/example/yukiacademy/dto/user/PaymentHistoryResponse.java

package org.example.yukiacademy.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta del historial de pagos de un usuario.
 * Representa una orden con información clave para mostrar al usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private Long orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status; // Ej. "PENDING", "APPROVED", "REJECTED"
    private String courseTitle; // Título del curso asociado a esta orden/pago
    // Puedes añadir más detalles si lo consideras necesario, como el ID de la transacción de MP
    private String mpPaymentId;
    private String mpPaymentStatus;
}
