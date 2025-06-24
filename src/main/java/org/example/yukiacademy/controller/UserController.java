package org.example.yukiacademy.controller;

import org.example.yukiacademy.dto.UserProfileDto;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Método para obtener el ID del usuario autenticado
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    // Obtener perfil del usuario autenticado
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados
    public ResponseEntity<UserProfileDto> getMyProfile() {
        UserProfileDto profile = userService.getUserProfile(getAuthenticatedUserId());
        return ResponseEntity.ok(profile);
    }

    // Actualizar perfil del usuario autenticado
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()") // Solo usuarios autenticados
    public ResponseEntity<UserProfileDto> updateMyProfile(@Valid @RequestBody UserProfileDto userProfileDto) {
        UserProfileDto updatedProfile = userService.updateUserProfile(getAuthenticatedUserId(), userProfileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    // Opcional: Obtener perfil de cualquier usuario por ID (solo para ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getUserProfileById(@PathVariable Long id) {
        UserProfileDto profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }

    // Opcional: Eliminar usuario (solo para ADMIN) - Ten cuidado con esto en producción
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // userService.deleteUser(id); // Necesitarías implementar este método en UserService
        return ResponseEntity.noContent().build();
    }
}