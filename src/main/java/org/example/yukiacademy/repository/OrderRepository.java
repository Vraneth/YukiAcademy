package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Encuentra todas las órdenes de un usuario específico
    List<Order> findByUser(User user);

    // Encuentra una orden por su ID y el usuario al que pertenece
    Optional<Order> findByIdAndUser(Long id, User user);

    // Puedes añadir otros métodos de búsqueda aquí si los necesitas,
    // por ejemplo, para buscar órdenes por estado, fecha, etc.
}