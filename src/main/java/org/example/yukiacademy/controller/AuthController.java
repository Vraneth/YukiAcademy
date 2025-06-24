package org.example.yukiacademy.controller;

import org.example.yukiacademy.dto.LoginRequest;
import org.example.yukiacademy.dto.RegisterRequest;
import org.example.yukiacademy.dto.AuthResponse;
import org.example.yukiacademy.service.AuthService;
import jakarta.validation.Valid; // Para la validación de DTOs
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/auth") // Prefijo para todas las rutas de este controlador
public class AuthController {

    private final AuthService authService;

    // Inyección de dependencia del AuthService
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register") // Maneja solicitudes POST a /api/auth/register
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // @Valid activa las validaciones definidas en el DTO (ej. @NotBlank, @Email)
        // @RequestBody mapea el cuerpo de la solicitud JSON a nuestro objeto RegisterRequest
        AuthResponse response = authService.registerUser(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED); // Devuelve 201 Created si el registro es exitoso
    }

    @PostMapping("/login") // Maneja solicitudes POST a /api/auth/login
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response); // Devuelve 200 OK si el login es exitoso
    }
}