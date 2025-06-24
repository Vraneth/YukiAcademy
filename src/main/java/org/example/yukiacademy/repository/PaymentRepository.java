package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Payment;
import org.example.yukiacademy.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Encuentra un pago asociado a una orden específica
    Optional<Payment> findByOrder(Order order);

    // Puedes añadir otros métodos de búsqueda aquí,
    // por ejemplo, findByTransactionId, findByStatus, etc.
}