package org.example.yukiacademy.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseSectionDto {
    private Long id;
    private String title;
    private Integer sectionOrder;
    private List<LessonDto> lessons;
}