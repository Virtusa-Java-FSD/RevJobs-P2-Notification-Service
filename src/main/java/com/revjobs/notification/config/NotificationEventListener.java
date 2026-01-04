package com.revjobs.notification.config;

import com.revjobs.notification.model.Notification;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.ZoneId;

@Component
public class NotificationEventListener extends AbstractMongoEventListener<Notification> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Notification> event) {
        Notification notification = event.getSource();

        // Auto-set timestamp before saving if not already set
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")));
        }

        // Auto-set isRead to false if not already set
        if (notification.getIsRead() == null) {
            notification.setIsRead(false);
        }
    }
}
