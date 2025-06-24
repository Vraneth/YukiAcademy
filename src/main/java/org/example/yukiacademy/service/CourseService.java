package org.example.yukiacademy.service;

import org.example.yukiacademy.model.Role; // Importación necesaria para usar Role.RoleName
import org.example.yukiacademy.dto.CourseDto;
import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.CourseRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository; // Para buscar al profesor

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public CourseDto createCourse(CourseDto courseDto, Long professorId) {
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesor no encontrado con ID: " + professorId));

        // Validar que el usuario sea realmente un profesor
        boolean isProfessor = professor.getRoles().stream()
                .anyMatch(role -> role.getName().equals(Role.RoleName.ROLE_PROFESSOR) || role.getName().equals(Role.RoleName.ROLE_ADMIN));
        if (!isProfessor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los profesores o administradores pueden crear cursos.");
        }

        Course course = new Course();
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setImageUrl(courseDto.getImageUrl());
        course.setPrice(courseDto.getPrice());
        course.setLanguage(courseDto.getLanguage());
        course.setLevel(courseDto.getLevel());
        course.setProfessor(professor);
        course.setCreatedAt(LocalDateTime.now());

        Course savedCourse = courseRepository.save(course);
        return convertToDto(savedCourse);
    }

    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado con ID: " + id));
        return convertToDto(course);
    }

    public CourseDto updateCourse(Long courseId, CourseDto courseDto, Long professorId) {
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado con ID: " + courseId));

        // Asegurarse de que el profesor que intenta actualizar el curso sea el dueño o un admin
        if (!existingCourse.getProfessor().getId().equals(professorId) && !userRepository.findById(professorId).get().getRoles().stream().anyMatch(r -> r.getName().equals(Role.RoleName.ROLE_ADMIN))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para actualizar este curso.");
        }

        existingCourse.setTitle(courseDto.getTitle());
        existingCourse.setDescription(courseDto.getDescription());
        existingCourse.setImageUrl(courseDto.getImageUrl());
        existingCourse.setPrice(courseDto.getPrice());
        existingCourse.setLanguage(courseDto.getLanguage());
        existingCourse.setLevel(courseDto.getLevel());
        existingCourse.setUpdatedAt(LocalDateTime.now());

        Course updatedCourse = courseRepository.save(existingCourse);
        return convertToDto(updatedCourse);
    }

    public void deleteCourse(Long courseId, Long professorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado con ID: " + courseId));

        // Asegurarse de que el profesor que intenta eliminar el curso sea el dueño o un admin
        if (!course.getProfessor().getId().equals(professorId) && !userRepository.findById(professorId).get().getRoles().stream().anyMatch(r -> r.getName().equals(Role.RoleName.ROLE_ADMIN))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este curso.");
        }
        courseRepository.delete(course);
    }

    // Método auxiliar para convertir entidad a DTO
    private CourseDto convertToDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setImageUrl(course.getImageUrl());
        dto.setPrice(course.getPrice());
        dto.setLanguage(course.getLanguage());
        dto.setLevel(course.getLevel());
        dto.setProfessorId(course.getProfessor().getId());
        dto.setProfessorName(course.getProfessor().getFirstName() + " " + course.getProfessor().getLastName());
        return dto;
    }
}