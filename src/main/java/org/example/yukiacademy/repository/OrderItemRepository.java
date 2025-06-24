package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Puedes añadir métodos de búsqueda personalizados si es necesario,
    // por ejemplo, findByOrderId para obtener todos los ítems de una orden.
}