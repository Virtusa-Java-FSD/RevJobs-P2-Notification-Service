package com.revjobs.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id; // MongoDB uses String IDs (ObjectId)

    @Indexed
    private Long userId;

    private String message;

    private String type;

    @Indexed
    private Boolean isRead = false;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    // Auto-set timestamp before saving
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
