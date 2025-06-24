package org.example.yukiacademy.service;

import org.example.yukiacademy.dto.UserProfileDto; // Crearemos este DTO
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class UserService { // Separamos la lógica de perfil de AuthService

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + userId));
        return convertToUserProfileDto(user);
    }

    public UserProfileDto updateUserProfile(Long userId, UserProfileDto userProfileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + userId));

        // Actualiza solo los campos permitidos para el perfil
        user.setFirstName(userProfileDto.getFirstName());
        user.setLastName(userProfileDto.getLastName());
        user.setProfilePictureUrl(userProfileDto.getProfilePictureUrl());
        user.setBio(userProfileDto.getBio());
        user.setInterests(userProfileDto.getInterests());
        user.setUpdatedAt(LocalDateTime.now()); // Actualiza la fecha de modificación

        User updatedUser = userRepository.save(user);
        return convertToUserProfileDto(updatedUser);
    }

    // Método auxiliar para convertir entidad a DTO de perfil
    private UserProfileDto convertToUserProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail()); // El email no se edita desde el perfil
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setBio(user.getBio());
        dto.setInterests(user.getInterests());
        // No se incluyen roles o contraseñas por seguridad
        return dto;
    }
}