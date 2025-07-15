package org.example.yukiacademy.service;

import org.example.yukiacademy.dto.CourseDto;
import org.example.yukiacademy.dto.UserProfileDto;
import org.example.yukiacademy.dto.user.ChangePasswordRequest;
import org.example.yukiacademy.dto.user.PaymentHistoryItemResponse;
import org.example.yukiacademy.dto.user.PaymentHistoryResponse;
import org.example.yukiacademy.dto.user.PrivacySettingsRequest;
import org.example.yukiacademy.dto.user.PrivacySettingsResponse;
import org.example.yukiacademy.dto.user.UpdateProfileRequest;
import org.example.yukiacademy.exception.UserNotFoundException;
import org.example.yukiacademy.model.Order;
import org.example.yukiacademy.model.Role;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.OrderRepository;
import org.example.yukiacademy.repository.RoleRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseService courseService;

    public UserService(UserRepository userRepository, OrderRepository orderRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, @Lazy CourseService courseService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        return convertUserToProfileDto(findUserById(userId));
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBio(request.getBio());
        user.setInterests(request.getInterests());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        return convertUserToProfileDto(userRepository.save(user));
    }

    @Transactional
    public UserProfileDto addProfessorRole(Long userId) {
        User user = findUserById(userId);
        Role professorRole = roleRepository.findByName(Role.RoleName.ROLE_PROFESSOR)
                .orElseThrow(() -> new RuntimeException("Error: Rol PROFESSOR no encontrado."));
        user.getRoles().add(professorRole);
        return convertUserToProfileDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Las nuevas contraseñas no coinciden.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getPurchasedCourses(Long userId) {
        User user = userRepository.findByIdWithPurchasedCourses(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        return user.getPurchasedCourses().stream()
                .map(courseService::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(Long userId) {
        List<Order> userOrders = orderRepository.findByUser(findUserById(userId));
        return userOrders.stream()
                .map(this::convertOrderToPaymentHistoryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrivacySettingsResponse getPrivacySettings(Long userId) {
        User user = findUserById(userId);
        return new PrivacySettingsResponse(user.getProfileVisibleToPublic(), user.getReceiveEmailNotifications());
    }

    @Transactional
    public PrivacySettingsResponse updatePrivacySettings(Long userId, PrivacySettingsRequest request) {
        User user = findUserById(userId);
        user.setProfileVisibleToPublic(request.getProfileVisibleToPublic());
        user.setReceiveEmailNotifications(request.getReceiveEmailNotifications());
        userRepository.save(user);
        return new PrivacySettingsResponse(user.getProfileVisibleToPublic(), user.getReceiveEmailNotifications());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    private UserProfileDto convertUserToProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setInterests(user.getInterests());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setProfileVisibleToPublic(user.getProfileVisibleToPublic());
        dto.setReceiveEmailNotifications(user.getReceiveEmailNotifications());
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()));
        }
        return dto;
    }

    private PaymentHistoryResponse convertOrderToPaymentHistoryDto(Order order) {
        PaymentHistoryResponse response = new PaymentHistoryResponse();
        response.setOrderId(order.getId());
        response.setOrderDate(order.getCreatedAt());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().toString());
        List<PaymentHistoryItemResponse> items = order.getOrderItems().stream().map(orderItem -> {
            PaymentHistoryItemResponse itemDto = new PaymentHistoryItemResponse();
            if (orderItem.getCourse() != null) {
                itemDto.setCourseId(orderItem.getCourse().getId());
                itemDto.setCourseTitle(orderItem.getCourse().getTitle());
            }
            itemDto.setPriceAtPurchase(orderItem.getPriceAtPurchase());
            return itemDto;
        }).collect(Collectors.toList());
        response.setItems(items);
        return response;
    }
}