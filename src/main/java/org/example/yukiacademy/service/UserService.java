// src/main/java/org/example/yukiacademy/service/UserService.java

package org.example.yukiacademy.service;

import org.example.yukiacademy.dto.user.ChangePasswordRequest;
import org.example.yukiacademy.dto.user.PaymentHistoryResponse;
import org.example.yukiacademy.dto.user.PrivacySettingsRequest;
import org.example.yukiacademy.dto.user.PrivacySettingsResponse;
import org.example.yukiacademy.dto.user.UpdateProfileRequest;
import org.example.yukiacademy.dto.UserProfileDto;
import org.example.yukiacademy.exception.ResourceNotFoundException;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.model.OrderItem;
import org.example.yukiacademy.model.Role; // Importar Role
import org.example.yukiacademy.repository.OrderRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.example.yukiacademy.repository.RoleRepository; // Importar RoleRepository
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set; // Importar Set
import java.util.HashSet; // Importar HashSet

// Opcionales si no estás implementando la subida de imagen en esta fase
// import org.springframework.web.multipart.MultipartFile;
// import java.io.IOException;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    // Si NO vas a implementar la subida de imagen de perfil en este sprint,
    // puedes COMENTAR o ELIMINAR la inyección de FileStorageService y sus usos.
    // private final FileStorageService fileStorageService;
    private final RoleRepository roleRepository; // <-- ¡NUEVA INYECCIÓN!

    // Constructor actualizado para incluir RoleRepository
    // Comenta/elimina FileStorageService si no lo necesitas ahora
    public UserService(UserRepository userRepository, OrderRepository orderRepository,
                       PasswordEncoder passwordEncoder, RoleRepository roleRepository /*, FileStorageService fileStorageService */) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository; // <-- ¡INICIALIZACIÓN!
        // this.fileStorageService = fileStorageService; // Comentar/eliminar si no se usa
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        logger.info("Obteniendo perfil para el usuario con ID: {}", userId);
        User user = getUserById(userId);
        return convertToUserProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        logger.info("Actualizando perfil para el usuario con ID: {}", userId);
        User user = getUserById(userId);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getProfilePictureUrl() != null) { // Mantener si `UpdateProfileRequest` tiene este campo
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        logger.info("Perfil actualizado exitosamente para el usuario con ID: {}", userId);
        return convertToUserProfileDto(updatedUser);
    }

    // Si estás quitando la funcionalidad de imagen de perfil por ahora,
    // puedes COMENTAR o ELIMINAR este método y sus dependencias (FileStorageService).
    /*
    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
        logger.info("Intentando subir imagen de perfil para el usuario con ID: {}", userId);
        User user = getUserById(userId);
        // ... (Tu lógica existente para uploadProfileImage con FileStorageService) ...
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            String oldObjectName = extractObjectNameFromUrl(user.getProfilePictureUrl());
            if (oldObjectName != null) {
                fileStorageService.deleteFile(oldObjectName);
            }
        }
        try {
            String objectName = fileStorageService.uploadFile(file);
            String imageUrl = fileStorageService.getFileUrl(objectName);
            user.setProfilePictureUrl(imageUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Imagen de perfil actualizada exitosamente para el usuario con ID: {}. URL: {}", userId, imageUrl);
            return imageUrl;
        } catch (IOException e) {
            logger.error("Error al procesar la imagen de perfil para el usuario con ID: {}", userId, e);
            throw new RuntimeException("Error al procesar la imagen de perfil: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al subir la imagen para el usuario con ID: {}", userId, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private String extractObjectNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        int lastSlashO = url.lastIndexOf("/o/");
        if (lastSlashO != -1 && lastSlashO + 3 < url.length()) {
            return url.substring(lastSlashO + 3);
        }
        return null;
    }
    */

    // NUEVO MÉTODO para añadir el rol de profesor al usuario
    @Transactional
    public UserProfileDto addProfessorRole(Long userId) {
        logger.info("Intentando añadir rol de profesor al usuario con ID: {}", userId);
        User user = getUserById(userId);

        Role professorRole = roleRepository.findByName(Role.RoleName.ROLE_PROFESSOR)
                .orElseThrow(() -> new ResourceNotFoundException("Rol de profesor no encontrado en la base de datos. Asegúrate de que exista."));

        if (user.getRoles().contains(professorRole)) {
            logger.warn("El usuario con ID {} ya tiene el rol de profesor. No se realizó ningún cambio.", userId);
            // Si el usuario ya es profesor, no hacemos nada y solo retornamos su perfil actual.
            // Puedes lanzar una excepción si prefieres un comportamiento más estricto.
        } else {
            user.getRoles().add(professorRole);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Rol de profesor añadido exitosamente al usuario con ID: {}", userId);
        }
        return convertToUserProfileDto(user); // Retorna el perfil actualizado con el nuevo rol
    }


    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        logger.info("Intentando cambiar contraseña para el usuario con ID: {}", userId);
        User user = getUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            logger.warn("Intento fallido de cambio de contraseña para usuario {}: Contraseña actual incorrecta.", userId);
            throw new IllegalArgumentException("Contraseña actual incorrecta.");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            logger.warn("Intento fallido de cambio de contraseña para usuario {}: Las nuevas contraseñas no coinciden.", userId);
            throw new IllegalArgumentException("Las nuevas contraseñas no coinciden.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        logger.info("Contraseña cambiada exitosamente para el usuario con ID: {}", userId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        logger.warn("Intentando eliminar cuenta del usuario con ID: {}", userId);
        User user = getUserById(userId);
        // Si estás quitando la funcionalidad de imagen de perfil por ahora,
        // puedes COMENTAR o ELIMINAR la lógica de eliminación de imagen.
        /*
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            String objectName = extractObjectNameFromUrl(user.getProfilePictureUrl());
            if (objectName != null) {
                fileStorageService.deleteFile(objectName);
            }
        }
        */
        userRepository.delete(user);
        logger.info("Cuenta eliminada exitosamente para el usuario con ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(Long userId) {
        logger.info("Obteniendo historial de pagos para el usuario con ID: {}", userId);
        User user = getUserById(userId);
        List<Order> userOrders = orderRepository.findByUser(user);

        return userOrders.stream()
                .map(order -> {
                    String courseTitle = "N/A";
                    if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                        OrderItem firstItem = order.getOrderItems().get(0);
                        if (firstItem.getCourse() != null) {
                            courseTitle = firstItem.getCourse().getTitle();
                        }
                    }

                    return new PaymentHistoryResponse(
                            order.getId(),
                            order.getOrderDate(),
                            order.getTotalAmount(),
                            order.getStatus().toString(),
                            courseTitle,
                            order.getMpPaymentId(),
                            order.getMpPaymentStatus()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrivacySettingsResponse getPrivacySettings(Long userId) {
        logger.info("Obteniendo configuración de privacidad para el usuario con ID: {}", userId);
        User user = getUserById(userId);
        return new PrivacySettingsResponse(
                user.getReceiveEmailNotifications(),
                user.getProfileVisibleToPublic()
        );
    }

    @Transactional
    public PrivacySettingsResponse updatePrivacySettings(Long userId, PrivacySettingsRequest request) {
        logger.info("Actualizando configuración de privacidad para el usuario con ID: {}", userId);
        User user = getUserById(userId);

        if (request.getReceiveEmailNotifications() != null) {
            user.setReceiveEmailNotifications(request.getReceiveEmailNotifications());
        }
        if (request.getProfileVisibleToPublic() != null) {
            user.setProfileVisibleToPublic(request.getProfileVisibleToPublic());
        }
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        logger.info("Configuración de privacidad actualizada exitosamente para el usuario con ID: {}", userId);
        return new PrivacySettingsResponse(user.getReceiveEmailNotifications(), user.getProfileVisibleToPublic());
    }

    // MODIFICACIÓN CRÍTICA AQUÍ: INCLUIR LOS ROLES EN EL DTO
    private UserProfileDto convertToUserProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        // Asegúrate de que profilePictureUrl sea null si está vacío (si lo usas)
        String profileUrl = user.getProfilePictureUrl();
        dto.setProfilePictureUrl(profileUrl != null && !profileUrl.isEmpty() ? profileUrl : null);
        dto.setBio(user.getBio());
        dto.setInterests(user.getInterests());

        // ¡AÑADIR ROLES AL DTO!
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name()) // Mapear Role a String del nombre
                .collect(Collectors.toSet()));

        return dto;
    }
}