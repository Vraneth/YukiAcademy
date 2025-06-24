package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime; // Para registrar la fecha de creación/última actualización
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", // Nombre de la tabla
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email") // Asegura que el email sea único
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // Usado para login

    @Column(nullable = false)
    private String password; // ¡Debe almacenarse cifrada!

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    // Relación ManyToMany con la entidad Role
    // fetch = FetchType.EAGER significa que los roles se cargarán inmediatamente con el usuario
    // @JoinTable define la tabla intermedia para la relación muchos a muchos
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "user_id"), // Columna que referencia al User
            inverseJoinColumns = @JoinColumn(name = "role_id") // Columna que referencia al Role
    )
    private Set<Role> roles = new HashSet<>(); // Usa HashSet para roles únicos

    // Campos adicionales para el perfil de usuario, según tus requerimientos
    @Column(name = "profile_picture_url")
    private String profilePictureUrl; // URL a la imagen de perfil
    private String bio; // Biografía o descripción corta
    private String interests; // Intereses del usuario

    // Campos de auditoría (opcionales pero recomendados para rastreo)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos de ciclo de vida de JPA para gestionar las fechas automáticamente
    @PrePersist // Se ejecuta antes de persistir una nueva entidad
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate // Se ejecuta antes de actualizar una entidad existente
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}