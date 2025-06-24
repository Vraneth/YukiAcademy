package org.example.yukiacademy.controller;

import org.example.yukiacademy.dto.CourseDto;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Método para obtener el ID del usuario autenticado
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    // Crear un nuevo curso (Solo PROFESOR o ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto createdCourse = courseService.createCourse(courseDto, getAuthenticatedUserId());
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    // Obtener todos los cursos (Público)
    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        List<CourseDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // Obtener un curso por ID (Público)
    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        CourseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    // Actualizar un curso (Solo PROFESOR o ADMIN, y si es el dueño del curso o un admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        CourseDto updatedCourse = courseService.updateCourse(id, courseDto, getAuthenticatedUserId());
        return ResponseEntity.ok(updatedCourse);
    }

    // Eliminar un curso (Solo PROFESOR o ADMIN, y si es el dueño del curso o un admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id, getAuthenticatedUserId());
        return ResponseEntity.noContent().build();
    }

    // TODO: Implementar endpoints para módulos, lecciones y contenido (subir archivos, etc.)
    // Estos serían más complejos y requerirían un servicio de almacenamiento de archivos (ej. S3, Cloudinary)
    // o manejo de archivos locales.
}