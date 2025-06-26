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

        // 4. Guardar el usuario en la base de datos
        userRepository.save(user);

        // 5. Autenticar y generar JWT inmediatamente después del registro
        // Esto automáticamente "loguea" al usuario recién creado.
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

        // 6. Devolver el AuthResponse con el token y la información del usuario
        return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getFirstName(), userDetails.getLastName(), userRoles);
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

        // 6. Devolver la respuesta con el JWT
        // userDetails.getUsername() por defecto es el email en UserDetailsImpl
        return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getFirstName(), userDetails.getLastName(), roles);
    }
}