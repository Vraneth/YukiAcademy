package org.example.yukiacademy.dto.user;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentHistoryItemResponse {
    private Long courseId;
    private String courseTitle;
    private BigDecimal priceAtPurchase;
}