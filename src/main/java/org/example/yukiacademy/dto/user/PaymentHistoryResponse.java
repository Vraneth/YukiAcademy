package org.example.yukiacademy.dto.user;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaymentHistoryResponse {
    private Long orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private List<PaymentHistoryItemResponse> items;
}