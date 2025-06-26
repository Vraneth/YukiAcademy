package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Course; // ¡Importación necesaria para la entidad Course!
import org.example.yukiacademy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Indica que esta interfaz es un componente de repositorio de Spring
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Métodos para buscar cursos por profesor, etc.
    List<Course> findByProfessor(User professor);
    // Puedes añadir más métodos de búsqueda aquí (por título, idioma, nivel, etc.)
    List<Course> findByTitleContainingIgnoreCase(String title);
}