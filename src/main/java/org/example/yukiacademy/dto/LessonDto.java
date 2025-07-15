package org.example.yukiacademy.dto;

import lombok.Data;
import org.example.yukiacademy.model.Lesson.LessonContentType;

@Data
public class LessonDto {
    private Long id;
    private String title;
    private LessonContentType contentType;
    private String videoUrl;
    private String articleContent;
    private Integer lessonOrder;
}