package org.example.yukiacademy.service;

import org.example.yukiacademy.dto.LoginRequest;
import org.example.yukiacademy.dto.RegisterRequest;
import org.example.yukiacademy.dto.AuthResponse;
import org.example.yukiacademy.model.Role;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.RoleRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.security.jwt.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado.");
        }

        User user = new User(
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword())
        );

        Set<Role> roles = new HashSet<>();
        Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Rol de estudiante no encontrado."));
        roles.add(studentRole);
        user.setRoles(roles);

        if (user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isEmpty()) {
            user.setProfilePictureUrl(null);
        }
        if (user.getBio() == null || user.getBio().isEmpty()) {
            user.setBio(null);
        }

        userRepository.save(user);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> userRoles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User registeredUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario registrado no encontrado"));

        return new AuthResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userRoles,
                registeredUser.getProfilePictureUrl(),
                registeredUser.getBio());
    }

    // Método de autenticación de usuario (login)
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User authenticatedUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));

        return new AuthResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                roles,
                authenticatedUser.getProfilePictureUrl(),
                authenticatedUser.getBio());
    }
}
