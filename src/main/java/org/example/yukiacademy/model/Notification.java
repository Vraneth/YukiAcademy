package org.example.yukiacademy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)

    @Column(name = "NOTIFICATION_TYPE", length = 50, nullable = false)
    private NotificationType type;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.type == null) {
            System.err.println("Advertencia: NotificationType es nulo en @PrePersist. Esto indica un posible error en el servicio de creación de notificaciones.");
        }
    }

    public enum NotificationType {
        NEW_COURSE,
        COURSE_UPDATE,
        PAYMENT_SUCCESS,
        PAYMENT_FAILURE,
        ROLE_CHANGE
    }
}