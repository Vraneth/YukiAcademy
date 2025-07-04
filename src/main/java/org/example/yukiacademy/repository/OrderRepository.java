// src/main/java/org/example/yukiacademy/repository/OrderRepository.java

package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.model.User; // Asegúrate de importar el modelo User
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Indica que esta interfaz es un componente de repositorio de Spring
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Encuentra todas las órdenes de un usuario específico
    List<Order> findByUser(User user);

    // Encuentra una orden por su ID y el usuario al que pertenece
    Optional<Order> findByIdAndUser(Long id, User user);

    // ¡NUEVO MÉTODO! Encuentra todas las órdenes por el ID del usuario
    List<Order> findByUserId(Long userId);

    // Puedes añadir otros métodos de búsqueda aquí si los necesitas,
    // por ejemplo, para buscar órdenes por estado, fecha, etc.
}
