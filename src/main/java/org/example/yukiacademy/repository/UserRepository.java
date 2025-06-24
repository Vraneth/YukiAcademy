package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Opcional, pero buena práctica

import java.util.Optional;

@Repository // Indica que esta interfaz es un componente de repositorio de Spring
public interface UserRepository extends JpaRepository<User, Long> {

    // Método personalizado para buscar un usuario por su email
    // Spring Data JPA lo implementará automáticamente
    Optional<User> findByEmail(String email);

    // Método para verificar si un email ya existe en la base de datos
    Boolean existsByEmail(String email);
}