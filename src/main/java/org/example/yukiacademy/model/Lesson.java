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
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se utiliza @Lob y columnDefinition para mapear a CLOB en Oracle para texto largo
    @Lob
    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(nullable = false, length = 200) // Mapea a VARCHAR2(200)
    private String title;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module; // Relación con la entidad Module
}