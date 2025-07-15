package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByProfessor(User professor);
    List<Course> findByTitleContainingIgnoreCase(String title);
}