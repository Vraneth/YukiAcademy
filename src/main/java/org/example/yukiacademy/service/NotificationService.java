package org.example.yukiacademy.service;

import org.example.yukiacademy.dto.NotificationDto;
import org.example.yukiacademy.exception.ResourceNotFoundException;
import org.example.yukiacademy.model.Course;
import org.example.yukiacademy.model.Notification;
import org.example.yukiacademy.model.Role;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.NotificationRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createNotificationForNewCourse(Course course) {
        logger.info("Creando notificaciones para el nuevo curso: {}", course.getTitle());
        List<User> students = userRepository.findByRoles_Name(Role.RoleName.ROLE_STUDENT);
        List<Notification> notificationsToSave = new ArrayList<>();

        String professorFirstName = "";
        String professorLastName = "";
        if (course.getProfessor() != null) {
            professorFirstName = course.getProfessor().getFirstName();
            professorLastName = course.getProfessor().getLastName();
        }

        for (User student : students) {
            if (course.getProfessor() == null || !student.getId().equals(course.getProfessor().getId())) {
                String message = String.format("¡Nuevo curso disponible! '%s' ha sido lanzado por %s %s.",
                        course.getTitle(), professorFirstName, professorLastName);
                Notification notification = new Notification();
                notification.setUser(student);
                notification.setMessage(message);
                notification.setType(Notification.NotificationType.NEW_COURSE);
                notification.setRelatedEntityId(course.getId());
                notification.setIsRead(false);
                notificationsToSave.add(notification);
            }
        }
        if (!notificationsToSave.isEmpty()) {
            notificationRepository.saveAll(notificationsToSave);
            logger.info("Generadas {} notificaciones de nuevo curso para '{}'.", notificationsToSave.size(), course.getTitle());
        } else {
            logger.info("No se generaron notificaciones de nuevo curso para '{}' ya que no hay estudiantes elegibles o el profesor es el único usuario.", course.getTitle());
        }
    }

    @Transactional
    public void createDirectNotification(User user, String message, Notification.NotificationType type, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setIsRead(false);
        notificationRepository.save(notification);
        logger.info("Notificación directa creada para usuario {}: {}", user.getEmail(), message);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationsCountForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public NotificationDto markNotificationAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            logger.warn("Usuario {} intentó marcar como leída la notificación {} que no le pertenece.", userId, notificationId);
            throw new ResourceNotFoundException("No tienes permiso para acceder a esta notificación.");
        }

        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        logger.info("Notificación {} marcada como leída para el usuario {}.", notificationId, userId);
        return convertToDto(updatedNotification);
    }

    @Transactional
    public long markAllNotificationsAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
        logger.info("{} notificaciones marcadas como leídas para el usuario {}.", unreadNotifications.size(), userId);
        return unreadNotifications.size();
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            logger.warn("Usuario {} intentó eliminar la notificación {} que no le pertenece.", userId, notificationId);
            throw new ResourceNotFoundException("No tienes permiso para eliminar esta notificación.");
        }

        notificationRepository.delete(notification);
        logger.info("Notificación {} eliminada para el usuario {}.", notificationId, userId);
    }

    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType().name());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}