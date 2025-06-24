package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order; // El pago está asociado a una orden

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus status; // PENDING, COMPLETED, FAILED, REFUNDED

    @Column(name = "transaction_id", length = 255)
    private String transactionId; // ID de la transacción de la pasarela de pago

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Ej: "credit_card", "paypal", "mercado_pago"

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @PrePersist
    protected void onCreate() {
        this.paymentDate = LocalDateTime.now();
    }
}