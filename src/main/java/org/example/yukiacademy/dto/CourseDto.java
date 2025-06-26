package org.example.yukiacademy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Importación para @NotNull
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
// ... otros imports ...
import org.example.yukiacademy.model.Course.CourseLevel; // Cambiar esta línea

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private Long id; // Para updates

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "El precio no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    private BigDecimal price;

    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String imageUrl;

    @NotBlank(message = "El idioma es obligatorio")
    private String language;

    @NotNull(message = "El nivel no puede ser nulo")
    private CourseLevel level;
    
    // ... resto del código ...

    private Long professorId; // ID del profesor
}