package org.example.yukiacademy.controller;

import org.example.yukiacademy.dto.LoginRequest;
import org.example.yukiacademy.dto.RegisterRequest;
import org.example.yukiacademy.dto.AuthResponse;
import org.example.yukiacademy.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // REST
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register") // registro
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.registerUser(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 Created registro exitoso
    }

    @PostMapping("/login") // login
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response); // 200 OK login exitoso
    }
}