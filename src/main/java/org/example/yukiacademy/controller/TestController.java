package org.example.yukiacademy.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/all")
    public String allAccess() {
        return "Contenido público.";
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT') or hasRole('PROFESSOR') or hasRole('ADMIN')")
    public String userAccess() {
        return "Contenido para estudiantes (o roles superiores).";
    }

    @GetMapping("/professor")
    @PreAuthorize("hasRole('PROFESSOR') or hasRole('ADMIN')")
    public String moderatorAccess() {
        return "Panel de profesor.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Panel de administrador.";
    }
}