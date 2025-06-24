package org.example.yukiacademy.service;

import org.example.yukiacademy.dto.LoginRequest;
import org.example.yukiacademy.dto.RegisterRequest;
import org.example.yukiacademy.dto.AuthResponse;
import org.example.yukiacademy.model.Role;
import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.RoleRepository;
import org.example.yukiacademy.repository.UserRepository;
import org.example.yukiacademy.security.details.UserDetailsImpl;
import org.example.yukiacademy.security.jwt.JwtUtils; // ¡Nueva importación!
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
    private final JwtUtils jwtUtils; // ¡Inyecta JwtUtils!

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) { // Añade JwtUtils al constructor
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils; // Asigna
    }

    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado.");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());

        Set<Role> roles = new HashSet<>();
        Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: Rol de estudiante no encontrado."));
        roles.add(studentRole);
        user.setRoles(roles);

        userRepository.save(user);

        List<String> userRoles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        // Opcional: Si quieres generar un token inmediatamente después del registro
        // Authentication authentication = authenticationManager.authenticate(
        //     new UsernamePasswordAuthenticationToken(registerRequest.getEmail(), registerRequest.getPassword())
        // );
        // SecurityContextHolder.getContext().setAuthentication(authentication);
        // String jwt = jwtUtils.generateJwtToken(authentication);
        // return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), userRoles);

        return new AuthResponse("Registro exitoso", user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), userRoles);
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // ¡Genera el JWT!
        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Devuelve la respuesta con el JWT
        return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getFirstName(), userDetails.getLastName(), roles);
    }
}