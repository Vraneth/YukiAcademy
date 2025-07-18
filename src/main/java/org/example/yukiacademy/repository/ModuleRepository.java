package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    // Puedes añadir métodos personalizados si los necesitas, ej: findByCourseId
}