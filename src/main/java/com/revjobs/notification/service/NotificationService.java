package com.revjobs.notification.service;

import com.revjobs.common.event.NotificationEvent;
import com.revjobs.common.exception.ResourceNotFoundException;
import com.revjobs.notification.model.Notification;
import com.revjobs.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Async
    @Transactional
    public void sendNotification(NotificationEvent event) {
        log.info("Sending notification to user: {}", event.getUserId());

        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setMessage(event.getMessage());
        notification.setType(event.getType());
        notification.setIsRead(false);

        notificationRepository.save(notification);

        log.info("Notification sent successfully");
    }

    @Transactional
    public Notification createNotification(Long userId, String message, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        unread.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllUserNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }
}
