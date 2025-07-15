package org.example.yukiacademy.controller;

import org.example.yukiacademy.dto.MessageResponse;
import org.example.yukiacademy.dto.NotificationDto;
import org.example.yukiacademy.exception.ResourceNotFoundException;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo determinar el ID de usuario desde el token.");
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDto>> getUserNotifications() {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadNotificationsCount() {
        Long userId = getAuthenticatedUserId();
        try {
            long count = notificationService.getUnreadNotificationsCountForUser(userId);
            return ResponseEntity.ok(count);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDto> markNotificationAsRead(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        try {
            NotificationDto updatedNotification = notificationService.markNotificationAsRead(id, userId);
            return ResponseEntity.ok(updatedNotification);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        try {
            notificationService.deleteNotification(id, userId);
            return ResponseEntity.ok(new MessageResponse("Notificación eliminada exitosamente."));
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}