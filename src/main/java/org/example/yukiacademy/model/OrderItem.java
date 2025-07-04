package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data; // Importa Lombok Data
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal; // Para el precio

@Entity
@Table(name = "order_items")
@Data // Lombok para getters y setters
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Relación con la orden
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER) // Relación con el curso
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer quantity; // ¡Este campo es CRÍTICO para setQuantity!

    @Column(name = "price_at_purchase", nullable = false, precision = 38, scale = 2)
    private BigDecimal priceAtPurchase; // Precio del curso en el momento de la compra
}