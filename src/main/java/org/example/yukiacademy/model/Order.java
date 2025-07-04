package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; // Importar ArrayList
import java.util.List;    // Importar List

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // ¡CAMBIO CLAVE AQUÍ! Asegurarse de que es List y que se inicializa como ArrayList
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>(); // Inicializar con ArrayList

    // Campos para la integración con Mercado Pago
    @Column(name = "mp_preference_id", length = 255)
    private String mpPreferenceId;

    @Column(name = "mp_payment_id", length = 255)
    private String mpPaymentId;

    @Column(name = "mp_payment_status", length = 50)
    private String mpPaymentStatus;

    @Column(name = "mp_payment_detail", length = 255)
    private String mpPaymentDetail;

    // Métodos de ciclo de vida JPA si los necesitas para auditoría
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now(); // Asegura una fecha de orden si no se establece
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}