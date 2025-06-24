package org.example.yukiacademy.dto;

import org.example.yukiacademy.model.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseDto {
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    private String title;

    @NotBlank(message = "La descripción no puede estar vacía")
    private String description;

    @NotBlank(message = "La URL de la imagen no puede estar vacía")
    private String imageUrl;

    @NotNull(message = "El precio no puede ser nulo")
    @Min(value = 0, message = "El precio debe ser un valor positivo")
    private BigDecimal price;

    @NotBlank(message = "El idioma no puede estar vacío")
    private String language;

    @NotNull(message = "El nivel no puede ser nulo")
    private CourseLevel level; // Usamos el enum CourseLevel

    private Long professorId; // ID del profesor
    private String professorName; // Nombre del profesor (para la respuesta)
}