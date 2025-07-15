package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    private BigDecimal price;
    private String imageUrl;
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_level")
    private CourseLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private User professor;

    private String category;
    private Boolean isPublished = false;

    @Lob
    private String summarySyllabusContent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sectionOrder ASC")
    private List<CourseSection> sections = new ArrayList<>();

    public enum CourseLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}