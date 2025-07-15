package org.example.yukiacademy.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String message;
    private String type;
    private Long relatedEntityId;
    private boolean isRead;
    private LocalDateTime createdAt;
}