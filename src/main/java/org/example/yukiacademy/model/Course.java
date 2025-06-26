package org.example.yukiacademy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob; // Importación necesaria para @Lob
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Se utiliza @Lob y columnDefinition para mapear a CLOB en Oracle para texto largo
    @Lob
    @Column(columnDefinition = "CLOB", nullable = false)
    private String description;

    @Column(name = "image_url", nullable = false, length = 255) // Mapea a VARCHAR2(255)
    private String imageUrl;

    @Column(nullable = false, length = 255) // Mapea a VARCHAR2(255)
    private String language;

    @Enumerated(EnumType.STRING) // Mapea el enum como String en la BD
    @Column(name = "course_level", length = 20) // <-- ¡CAMBIO CLAVE AQUÍ: renombramos 'level' a 'course_level'!
    private CourseLevel level; // Referencia al enum CourseLevel (que es anidado en esta clase)

    @Column(nullable = false, precision = 38, scale = 2) // Mapea a NUMBER(38,2)
    private BigDecimal price;

    @Column(nullable = false, length = 200) // Mapea a VARCHAR2(200)
    private String title;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor; // Relación con la entidad User (profesor)

    // ¡La definición del enum CourseLevel como clase anidada!
    public enum CourseLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS
    }
}