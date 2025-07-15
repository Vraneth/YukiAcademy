package org.example.yukiacademy.controller;

import org.example.yukiacademy.dto.CourseDto;
import org.example.yukiacademy.dto.MessageResponse;
import org.example.yukiacademy.dto.user.ChangePasswordRequest;
import org.example.yukiacademy.dto.user.PaymentHistoryResponse;
import org.example.yukiacademy.dto.user.PrivacySettingsRequest;
import org.example.yukiacademy.dto.user.PrivacySettingsResponse;
import org.example.yukiacademy.dto.user.UpdateProfileRequest;
import org.example.yukiacademy.dto.UserProfileDto;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.service.UserService;
import org.example.yukiacademy.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            return userDetails.getId();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: Tipo de usuario no reconocido.");
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getMyProfile() {
        UserProfileDto profile = userService.getUserProfile(getAuthenticatedUserId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getAuthenticatedUserId();
        UserProfileDto updatedProfile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/become-professor")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> becomeProfessor() {
        Long userId = getAuthenticatedUserId();
        try {
            UserProfileDto updatedProfile = userService.addProfessorRole(userId);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getAuthenticatedUserId();
        try {
            userService.changePassword(userId, request);
            return ResponseEntity.ok(new MessageResponse("Contraseña actualizada exitosamente."));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("isAuthenticated() and #userId == authentication.principal.id")
    public ResponseEntity<MessageResponse> deleteAccount(@PathVariable Long userId) {
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!authenticatedUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta cuenta.");
        }
        userService.deleteUser(userId);
        return ResponseEntity.ok(new MessageResponse("Cuenta eliminada exitosamente."));
    }

    @GetMapping("/{userId}/purchased-courses")
    @PreAuthorize("isAuthenticated() and #userId == authentication.principal.id")
    public ResponseEntity<List<CourseDto>> getPurchasedCourses(@PathVariable Long userId) {
        try {
            List<CourseDto> purchasedCourses = userService.getPurchasedCourses(userId);
            return ResponseEntity.ok(purchasedCourses);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener cursos comprados: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/payments")
    @PreAuthorize("isAuthenticated() and #userId == authentication.principal.id")
    public ResponseEntity<List<PaymentHistoryResponse>> getUserPaymentHistory(@PathVariable Long userId) {
        Long authenticatedUserId = getAuthenticatedUserId();
        if (!authenticatedUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este historial.");
        }
        List<PaymentHistoryResponse> history = userService.getPaymentHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/privacy-settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrivacySettingsResponse> getPrivacySettings() {
        Long userId = getAuthenticatedUserId();
        PrivacySettingsResponse settings = userService.getPrivacySettings(userId);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/privacy-settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrivacySettingsResponse> updatePrivacySettings(@RequestBody PrivacySettingsRequest request) {
        Long userId = getAuthenticatedUserId();
        PrivacySettingsResponse updatedSettings = userService.updatePrivacySettings(userId, request);
        return ResponseEntity.ok(updatedSettings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getUserProfileById(@PathVariable Long id) {
        UserProfileDto profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> adminDeleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("Usuario eliminado por administrador exitosamente."));
    }
}