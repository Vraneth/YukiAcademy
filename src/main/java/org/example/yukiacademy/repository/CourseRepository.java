package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Métodos para buscar cursos por profesor, etc.
    List<Course> findByProfessor(User professor);
    // Puedes añadir más métodos de búsqueda aquí (por título, idioma, nivel, etc.)
    List<Course> findByTitleContainingIgnoreCase(String title);
}