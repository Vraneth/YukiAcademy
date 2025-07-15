package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Enumerated(EnumType.STRING) private LessonContentType contentType;
    @Lob private String videoUrl;
    @Lob private String articleContent;
    private Integer lessonOrder;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    public enum LessonContentType { VIDEO, ARTICLE }

    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}