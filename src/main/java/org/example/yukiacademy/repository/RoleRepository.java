package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Método personalizado para buscar un rol por su nombre (ej. ROLE_STUDENT)
    Optional<Role> findByName(Role.RoleName name);
}