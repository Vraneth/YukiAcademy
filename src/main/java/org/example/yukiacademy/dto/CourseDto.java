package org.example.yukiacademy.dto;

import lombok.Data;
import org.example.yukiacademy.model.Course.CourseLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String language;
    private CourseLevel level;
    private Long professorId;
    private String professorFirstName;
    private String professorLastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;
    private Boolean isPublished;
    private String summarySyllabus;
    private List<CourseSectionDto> sections;
}