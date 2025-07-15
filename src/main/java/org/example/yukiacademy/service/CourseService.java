package org.example.yukiacademy.service;

import org.example.yukiacademy.dto.CourseDto;
import org.example.yukiacademy.dto.CourseSectionDto;
import org.example.yukiacademy.dto.LessonDto;
import org.example.yukiacademy.exception.CourseNotFoundException;
import org.example.yukiacademy.exception.UserNotFoundException;
import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.CourseSection;
import org.example.yukiacademy.model.Lesson;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.CourseRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository, NotificationService notificationService) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public CourseDto createCourse(CourseDto courseDto, Long professorId) {
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new UserNotFoundException("Profesor no encontrado con ID: " + professorId));

        Course course = new Course();
        course.setProfessor(professor);
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setPrice(courseDto.getPrice());
        course.setImageUrl(courseDto.getImageUrl());
        course.setLanguage(courseDto.getLanguage());
        course.setLevel(courseDto.getLevel());
        course.setCategory(courseDto.getCategory());
        course.setSummarySyllabusContent(courseDto.getSummarySyllabus());
        course.setIsPublished(false);

        Course savedCourse = courseRepository.save(course);
        return convertToDto(savedCourse);
    }

    @Transactional
    public CourseDto updateCourse(Long id, CourseDto courseDto, Long authenticatedUserId) {
        Course courseToUpdate = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Curso no encontrado con ID: " + id));

        if (!courseToUpdate.getProfessor().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("No tienes permiso para actualizar este curso.");
        }

        courseToUpdate.setTitle(courseDto.getTitle());
        courseToUpdate.setDescription(courseDto.getDescription());
        courseToUpdate.setPrice(courseDto.getPrice());
        courseToUpdate.setImageUrl(courseDto.getImageUrl());
        courseToUpdate.setLanguage(courseDto.getLanguage());
        courseToUpdate.setLevel(courseDto.getLevel());
        courseToUpdate.setCategory(courseDto.getCategory());
        courseToUpdate.setSummarySyllabusContent(courseDto.getSummarySyllabus());

        if (courseDto.getSections() != null) {
            courseToUpdate.getSections().clear();
            List<CourseSection> newSections = courseDto.getSections().stream()
                    .map(sectionDto -> convertSectionDtoToEntity(sectionDto, courseToUpdate))
                    .collect(Collectors.toList());
            courseToUpdate.getSections().addAll(newSections);
        }

        Course updatedCourse = courseRepository.save(courseToUpdate);
        return convertToDto(updatedCourse);
    }

    @Transactional
    public CourseDto publishCourse(Long courseId, Long authenticatedUserId, boolean publish) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Curso no encontrado con ID: " + courseId));

        if (!course.getProfessor().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("No tienes permiso para publicar este curso.");
        }

        boolean wasPublished = course.getIsPublished() != null && course.getIsPublished();

        course.setIsPublished(publish);
        Course updatedCourse = courseRepository.save(course);

        if (publish && !wasPublished) {
            notificationService.createNotificationForNewCourse(updatedCourse);
        }

        return convertToDto(updatedCourse);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Curso no encontrado con ID: " + id));
        return convertToDto(course);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getCoursesByProfessorId(Long professorId) {
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new UserNotFoundException("Profesor no encontrado con ID: " + professorId));
        return courseRepository.findByProfessor(professor).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCourse(Long courseId, Long authenticatedUserId) {
        Course courseToDelete = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Curso no encontrado con ID: " + courseId));

        User authenticatedUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuario autenticado no encontrado"));

        boolean isAdmin = authenticatedUser.getRoles().stream()
                .anyMatch(role -> role.getName().toString().equals("ROLE_ADMIN"));

        if (!courseToDelete.getProfessor().getId().equals(authenticatedUserId) && !isAdmin) {
            throw new AccessDeniedException("No tienes permiso para eliminar este curso.");
        }
        courseRepository.delete(courseToDelete);
    }

    public CourseDto convertToDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setPrice(course.getPrice());
        dto.setImageUrl(course.getImageUrl());
        dto.setLanguage(course.getLanguage());
        dto.setLevel(course.getLevel());
        dto.setCategory(course.getCategory());
        dto.setIsPublished(course.getIsPublished());
        dto.setSummarySyllabus(course.getSummarySyllabusContent());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());

        if (course.getProfessor() != null) {
            dto.setProfessorId(course.getProfessor().getId());
            dto.setProfessorFirstName(course.getProfessor().getFirstName());
            dto.setProfessorLastName(course.getProfessor().getLastName());
        }

        if (course.getSections() != null) {
            dto.setSections(course.getSections().stream()
                    .map(this::convertSectionEntityToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private CourseSectionDto convertSectionEntityToDto(CourseSection section) {
        CourseSectionDto dto = new CourseSectionDto();
        dto.setId(section.getId());
        dto.setTitle(section.getTitle());
        dto.setSectionOrder(section.getSectionOrder());
        if (section.getLessons() != null) {
            dto.setLessons(section.getLessons().stream()
                    .map(this::convertLessonEntityToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private LessonDto convertLessonEntityToDto(Lesson lesson) {
        LessonDto dto = new LessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContentType(lesson.getContentType());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setArticleContent(lesson.getArticleContent());
        dto.setLessonOrder(lesson.getLessonOrder());
        return dto;
    }

    private CourseSection convertSectionDtoToEntity(CourseSectionDto sectionDto, Course course) {
        CourseSection section = new CourseSection();
        if (sectionDto.getId() != null && !String.valueOf(sectionDto.getId()).contains("temp")) {
            section.setId(sectionDto.getId());
        }
        section.setTitle(section.getTitle());
        section.setSectionOrder(section.getSectionOrder());
        section.setCourse(course);
        if (sectionDto.getLessons() != null) {
            List<Lesson> lessons = sectionDto.getLessons().stream()
                    .map(lessonDto -> convertLessonDtoToEntity(lessonDto, section))
                    .collect(Collectors.toList());
            section.setLessons(lessons);
        }
        return section;
    }

    private Lesson convertLessonDtoToEntity(LessonDto lessonDto, CourseSection section) {
        Lesson lesson = new Lesson();
        if (lessonDto.getId() != null && !String.valueOf(lessonDto.getId()).contains("temp")) {
            lesson.setId(lessonDto.getId());
        }
        lesson.setTitle(lessonDto.getTitle());
        lesson.setContentType(lessonDto.getContentType());
        lesson.setVideoUrl(lesson.getVideoUrl());
        lesson.setArticleContent(lessonDto.getArticleContent());
        lesson.setLessonOrder(lesson.getLessonOrder());
        lesson.setSection(section);
        return lesson;
    }
}