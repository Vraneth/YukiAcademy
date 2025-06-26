package org.example.yukiacademy.service;

import org.example.yukiacademy.dto.CourseDto;
import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.CourseLevel; // Importación del enum CourseLevel
import org.example.yukiacademy.model.User; // Importación de la entidad User
import org.example.yukiacademy.repository.CourseRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service // Anotación para que Spring lo reconozca como un bean de servicio
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository; // Inyectamos UserRepository para encontrar al profesor

    // Constructor para inyección de dependencias
    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crea un nuevo curso en la base de datos.
     * @param courseDto El DTO que contiene los datos del curso a crear.
     * @param professorId El ID del profesor que crea el curso (autenticado).
     * @return El CourseDto del curso creado.
     * @throws ResponseStatusException si el profesor no es encontrado.
     */
    @Transactional // Marca el método como transaccional
    public CourseDto createCourse(CourseDto courseDto, Long professorId) {
        // Buscar al profesor por su ID
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profesor no encontrado con ID: " + professorId));

        Course course = new Course();
        // Mapear propiedades de CourseDto a la entidad Course
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setPrice(courseDto.getPrice());
        course.setImageUrl(courseDto.getImageUrl());
        course.setLanguage(courseDto.getLanguage());
        course.setLevel(courseDto.getLevel()); // Usamos el enum CourseLevel
        course.setProfessor(professor); // Asignar el profesor
        course.setCreatedAt(LocalDateTime.now()); // Establecer la fecha de creación
        course.setUpdatedAt(LocalDateTime.now()); // Establecer la fecha de actualización

        Course savedCourse = courseRepository.save(course);
        return convertToDto(savedCourse); // Convertir la entidad guardada a DTO y devolver
    }

    /**
     * Obtiene una lista de todos los cursos disponibles.
     * @return Una lista de CourseDto.
     */
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDto) // Convertir cada entidad a DTO
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un curso específico por su ID.
     * @param id El ID del curso.
     * @return El CourseDto del curso.
     * @throws ResponseStatusException si el curso no es encontrado.
     */
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado con ID: " + id));
        return convertToDto(course);
    }

    /**
     * Actualiza un curso existente.
     * @param id El ID del curso a actualizar.
     * @param courseDto El DTO con los datos actualizados del curso.
     * @param authenticatedUserId El ID del usuario autenticado (para verificar permisos).
     * @return El CourseDto del curso actualizado.
     * @throws ResponseStatusException si el curso no es encontrado o el usuario no tiene permisos.
     */
    @Transactional
    public CourseDto updateCourse(Long id, CourseDto courseDto, Long authenticatedUserId) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado con ID: " + id));

        // Verificar si el usuario autenticado es el profesor del curso o un ADMIN
        if (!existingCourse.getProfessor().getId().equals(authenticatedUserId) && !userRepository.findById(authenticatedUserId).get().getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para actualizar este curso.");
        }

        // Actualizar propiedades
        existingCourse.setTitle(courseDto.getTitle());
        existingCourse.setDescription(courseDto.getDescription());
        existingCourse.setPrice(courseDto.getPrice());
        existingCourse.setImageUrl(courseDto.getImageUrl());
        existingCourse.setLanguage(courseDto.getLanguage());
        existingCourse.setLevel(courseDto.getLevel()); // Actualizar el nivel
        existingCourse.setUpdatedAt(LocalDateTime.now()); // Actualizar fecha de modificación

        Course updatedCourse = courseRepository.save(existingCourse);
        return convertToDto(updatedCourse);
    }

    /**
     * Elimina un curso por su ID.
     * @param id El ID del curso a eliminar.
     * @param authenticatedUserId El ID del usuario autenticado (para verificar permisos).
     * @throws ResponseStatusException si el curso no es encontrado o el usuario no tiene permisos.
     */
    @Transactional
    public void deleteCourse(Long id, Long authenticatedUserId) {
        Course courseToDelete = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado con ID: " + id));

        // Verificar si el usuario autenticado es el profesor del curso o un ADMIN
        if (!courseToDelete.getProfessor().getId().equals(authenticatedUserId) && !userRepository.findById(authenticatedUserId).get().getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este curso.");
        }

        courseRepository.delete(courseToDelete);
    }

    /**
     * Método auxiliar para convertir una entidad Course a un DTO CourseDto.
     * @param course La entidad Course.
     * @return El CourseDto correspondiente.
     */
    private CourseDto convertToDto(Course course) {
        CourseDto courseDto = new CourseDto();
        courseDto.setId(course.getId());
        courseDto.setTitle(course.getTitle());
        courseDto.setDescription(course.getDescription());
        courseDto.setPrice(course.getPrice());
        courseDto.setImageUrl(course.getImageUrl());
        courseDto.setLanguage(course.getLanguage());
        courseDto.setLevel(course.getLevel()); // Usamos el enum CourseLevel
        courseDto.setProfessorId(course.getProfessor().getId()); // Aseguramos que el ID del profesor se mapee

        return courseDto;
    }
}