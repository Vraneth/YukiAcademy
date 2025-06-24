package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ContentType type; // VIDEO, PDF, TEXT, LINK

    @Column(columnDefinition = "TEXT")
    private String contentUrl; // URL del video, PDF, o el texto/link en sí

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "order_index")
    private Integer orderIndex; // Para el orden de los contenidos dentro de la lección
}