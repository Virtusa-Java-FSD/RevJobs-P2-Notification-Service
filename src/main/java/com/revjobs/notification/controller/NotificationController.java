package com.revjobs.notification.controller;

import com.revjobs.common.dto.ApiResponse;
import com.revjobs.common.event.NotificationEvent;
import com.revjobs.notification.model.Notification;
import com.revjobs.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendNotification(@RequestBody NotificationEvent event) {
        notificationService.sendNotification(event);
        return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Notification>> createNotification(
            @RequestParam Long userId,
            @RequestParam String message,
            @RequestParam String type) {
        Notification notification = notificationService.createNotification(userId, message, type);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created", notification));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable String notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteAllUserNotifications(@PathVariable Long userId) {
        notificationService.deleteAllUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications deleted", null));
    }
}
