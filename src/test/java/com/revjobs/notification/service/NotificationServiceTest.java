package com.revjobs.notification.service;

import com.revjobs.common.event.NotificationEvent;
import com.revjobs.common.exception.ResourceNotFoundException;
import com.revjobs.notification.model.Notification;
import com.revjobs.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

        @Mock
        private NotificationRepository notificationRepository;

        @InjectMocks
        private NotificationService notificationService;

        private Notification testNotification;
        private NotificationEvent testEvent;

        @BeforeEach
        void setUp() {
                testNotification = new Notification();
                testNotification.setId("notif123");
                testNotification.setUserId(100L);
                testNotification.setMessage("Application submitted successfully");
                testNotification.setType("APPLICATION_SUBMITTED");
                testNotification.setIsRead(false);
                testNotification.setCreatedAt(ZonedDateTime.now(ZoneId.of("UTC")));

                testEvent = new NotificationEvent();
                testEvent.setUserId(100L);
                testEvent.setMessage("Application submitted successfully");
                testEvent.setType("APPLICATION_SUBMITTED");
                testEvent.setTimestamp(LocalDateTime.now());
        }

        @Test
        void sendNotification_CreatesAndSavesNotification() {
                // Given
                when(notificationRepository.save(any(Notification.class)))
                                .thenReturn(testNotification);

                // When
                notificationService.sendNotification(testEvent);

                // Then
                verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        void createNotification_Success() {
                // Given
                when(notificationRepository.save(any(Notification.class)))
                                .thenReturn(testNotification);

                // When
                Notification result = notificationService.createNotification(
                                100L, "Test message", "TEST_TYPE");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getUserId()).isEqualTo(100L);
                assertThat(result.getMessage()).isEqualTo("Application submitted successfully");
                verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        void getUserNotifications_ReturnsUserNotifications() {
                // Given
                Notification notif2 = new Notification();
                notif2.setId("notif456");
                notif2.setUserId(100L);
                notif2.setMessage("Status updated");

                List<Notification> notifications = Arrays.asList(testNotification, notif2);
                when(notificationRepository.findByUserIdOrderByCreatedAtDesc(100L))
                                .thenReturn(notifications);

                // When
                List<Notification> result = notificationService.getUserNotifications(100L);

                // Then
                assertThat(result).hasSize(2);
                assertThat(result).containsExactly(testNotification, notif2);
                verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(100L);
        }

        @Test
        void getUnreadNotifications_ReturnsOnlyUnread() {
                // Given
                List<Notification> unreadNotifications = Arrays.asList(testNotification);
                when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(100L))
                                .thenReturn(unreadNotifications);

                // When
                List<Notification> result = notificationService.getUnreadNotifications(100L);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getIsRead()).isFalse();
                verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(100L);
        }

        @Test
        void getUnreadCount_ReturnsCount() {
                // Given
                when(notificationRepository.countByUserIdAndIsReadFalse(100L))
                                .thenReturn(5L);

                // When
                long count = notificationService.getUnreadCount(100L);

                // Then
                assertThat(count).isEqualTo(5L);
                verify(notificationRepository).countByUserIdAndIsReadFalse(100L);
        }

        @Test
        void markAsRead_Success() {
                // Given
                when(notificationRepository.findById("notif123"))
                                .thenReturn(Optional.of(testNotification));

                Notification readNotification = new Notification();
                readNotification.setId("notif123");
                readNotification.setIsRead(true);
                readNotification.setReadAt(ZonedDateTime.now(ZoneId.of("UTC")));

                when(notificationRepository.save(any(Notification.class)))
                                .thenReturn(readNotification);

                // When
                Notification result = notificationService.markAsRead("notif123");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getIsRead()).isTrue();
                assertThat(result.getReadAt()).isNotNull();
                verify(notificationRepository).findById("notif123");
                verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        void markAsRead_ThrowsResourceNotFoundException_WhenNotFound() {
                // Given
                when(notificationRepository.findById("invalid"))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> notificationService.markAsRead("invalid"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Notification not found");

                verify(notificationRepository).findById("invalid");
                verify(notificationRepository, never()).save(any());
        }

        @Test
        void markAllAsRead_MarksAllUnreadAsRead() {
                // Given
                Notification notif1 = new Notification();
                notif1.setId("notif1");
                notif1.setUserId(100L);
                notif1.setIsRead(false);

                Notification notif2 = new Notification();
                notif2.setId("notif2");
                notif2.setUserId(100L);
                notif2.setIsRead(false);

                List<Notification> unreadNotifications = Arrays.asList(notif1, notif2);
                when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(100L))
                                .thenReturn(unreadNotifications);
                when(notificationRepository.saveAll(any()))
                                .thenReturn(unreadNotifications);

                // When
                notificationService.markAllAsRead(100L);

                // Then
                verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(100L);
                verify(notificationRepository).saveAll(unreadNotifications);
        }

        @Test
        void deleteNotification_Success() {
                // Given
                when(notificationRepository.findById("notif123"))
                                .thenReturn(Optional.of(testNotification));
                doNothing().when(notificationRepository).delete(testNotification);

                // When
                notificationService.deleteNotification("notif123");

                // Then
                verify(notificationRepository).findById("notif123");
                verify(notificationRepository).delete(testNotification);
        }

        @Test
        void deleteNotification_ThrowsResourceNotFoundException_WhenNotFound() {
                // Given
                when(notificationRepository.findById("invalid"))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> notificationService.deleteNotification("invalid"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessage("Notification not found");

                verify(notificationRepository).findById("invalid");
                verify(notificationRepository, never()).delete(any());
        }

        @Test
        void deleteAllUserNotifications_DeletesAllForUser() {
                // Given
                doNothing().when(notificationRepository).deleteByUserId(100L);

                // When
                notificationService.deleteAllUserNotifications(100L);

                // Then
                verify(notificationRepository).deleteByUserId(100L);
        }
}
