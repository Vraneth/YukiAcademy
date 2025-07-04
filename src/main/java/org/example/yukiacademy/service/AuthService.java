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
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Importa esta excepción
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

    // Método de registro de usuario
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        // 1. Verificar si el email ya existe
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado.");
        }

        // 2. Crear una nueva instancia de User utilizando el constructor con nombre y apellido
        User user = new User(
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword())
        );

        // 3. Asignar roles por defecto (ej. ROLE_STUDENT)
        Set<Role> roles = new HashSet<>();
        Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Rol de estudiante no encontrado."));
        roles.add(studentRole);
        user.setRoles(roles);

        // Establecer valores por defecto para profilePictureUrl y bio si es un nuevo registro y no se proporcionan
        // Esto es opcional, pero asegura que no sean null en el objeto User si el frontend los espera
        if (user.getProfilePictureUrl() == null || user.getProfilePictureUrl().isEmpty()) {
            user.setProfilePictureUrl(null); // O una URL de imagen por defecto
        }
        if (user.getBio() == null || user.getBio().isEmpty()) {
            user.setBio(null); // O una biografía por defecto
        }

        // 4. Guardar el usuario en la base de datos
        userRepository.save(user); // Guarda el usuario con los roles y, opcionalmente, los valores por defecto de perfil/bio

        // 5. Autenticar y generar JWT inmediatamente después del registro
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generar el token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Obtener los roles del usuario para la respuesta
        List<String> userRoles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Para obtener profilePictureUrl y bio, necesitamos el objeto User completo
        // userDetails no contiene estos campos por defecto
        User registeredUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario registrado no encontrado"));

        // 6. Devolver el AuthResponse con el token y la información completa del usuario
        return new AuthResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(), // Email
                userDetails.getFirstName(), // Nombre (si UserDetailsImpl lo tiene)
                userDetails.getLastName(),  // Apellido (si UserDetailsImpl lo tiene)
                userRoles,
                registeredUser.getProfilePictureUrl(), // <-- ¡AÑADIDO!
                registeredUser.getBio());             // <-- ¡AÑADIDO!
    }

    // Método de autenticación de usuario (login)
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        // 1. Autenticar al usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // 2. Establecer la autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Obtener los detalles del usuario autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 4. Generar el token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 5. Obtener los roles del usuario para la respuesta
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Para obtener profilePictureUrl y bio, necesitamos el objeto User completo
        // userDetails no contiene estos campos por defecto
        User authenticatedUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado"));

        // 6. Devolver la respuesta con el JWT y la información completa del perfil
        return new AuthResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(), // Email
                userDetails.getFirstName(), // Nombre
                userDetails.getLastName(),  // Apellido
                roles,
                authenticatedUser.getProfilePictureUrl(), // <-- ¡AÑADIDO!
                authenticatedUser.getBio());             // <-- ¡AÑADIDO!
    }
}
