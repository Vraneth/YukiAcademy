package org.example.yukiacademy.controller;

import org.example.yukiacademy.dto.CourseDto;
import org.example.yukiacademy.dto.MessageResponse;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.service.CourseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.example.yukiacademy.exception.CourseNotFoundException;
import org.example.yukiacademy.exception.UserNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        } else {
            logger.error("El principal de autenticación no es UserDetailsImpl. Tipo: {}", principal.getClass().getName());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo determinar el ID de usuario desde el token.");
        }
    }

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        logger.info("Solicitud para obtener todos los cursos.");
        List<CourseDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        logger.info("Solicitud para obtener curso con ID: {}", id);
        try {
            CourseDto course = courseService.getCourseById(id);
            return ResponseEntity.ok(course);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CourseDto>> getMyCourses() {
        logger.info("Solicitud para obtener cursos del profesor autenticado.");
        Long authenticatedUserId = getAuthenticatedUserId();
        try {
            List<CourseDto> myCourses = courseService.getCoursesByProfessorId(authenticatedUserId);
            return ResponseEntity.ok(myCourses);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseDto courseDto) {
        logger.info("Solicitud para crear nuevo curso: {}", courseDto.getTitle());
        Long professorId = getAuthenticatedUserId();
        try {
            CourseDto createdCourse = courseService.createCourse(courseDto, professorId);
            return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        logger.info("Solicitud para actualizar curso con ID: {}", id);
        Long authenticatedUserId = getAuthenticatedUserId();
        try {
            CourseDto updatedCourse = courseService.updateCourse(id, courseDto, authenticatedUserId);
            return ResponseEntity.ok(updatedCourse);
        } catch (CourseNotFoundException | UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    // --- MÉTODO AÑADIDO ---
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<CourseDto> publishCourse(@PathVariable Long id, @RequestParam boolean publish) {
        logger.info("Solicitud para {} curso con ID: {}", (publish ? "publicar" : "despublicar"), id);
        Long authenticatedUserId = getAuthenticatedUserId();
        try {
            CourseDto updatedCourse = courseService.publishCourse(id, authenticatedUserId, publish);
            return ResponseEntity.ok(updatedCourse);
        } catch (CourseNotFoundException | UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCourse(@PathVariable Long id) {
        logger.info("Solicitud para eliminar curso con ID: {}", id);
        Long authenticatedUserId = getAuthenticatedUserId();
        try {
            courseService.deleteCourse(id, authenticatedUserId);
            return ResponseEntity.ok(new MessageResponse("Curso eliminado exitosamente."));
        } catch (CourseNotFoundException | UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}