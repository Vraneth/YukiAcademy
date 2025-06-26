package org.example.yukiacademy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se utiliza @Lob y columnDefinition para mapear a CLOB en Oracle para texto largo
    @Lob
    @Column(name = "content_url", columnDefinition = "CLOB")
    private String contentUrl;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(nullable = false, length = 100) // Mapea a VARCHAR2(100) en Oracle
    private String title;

    @Column(nullable = false, length = 20) // Mapea a VARCHAR2(20) en Oracle
    // Se asume que 'type' es un String. Si es un enum, se usaría @Enumerated(EnumType.STRING)
    private String type;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson; // Relación con la entidad Lesson
}