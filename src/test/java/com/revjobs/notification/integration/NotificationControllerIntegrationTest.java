package com.revjobs.notification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revjobs.notification.model.Notification;
import com.revjobs.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void createNotification_Success() throws Exception {
        mockMvc.perform(post("/notifications")
                .param("userId", "100")
                .param("message", "Test notification")
                .param("type", "TEST_TYPE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(100))
                .andExpect(jsonPath("$.data.message").value("Test notification"))
                .andExpect(jsonPath("$.data.type").value("TEST_TYPE"))
                .andExpect(jsonPath("$.data.isRead").value(false));
    }

    @Test
    void getUserNotifications_ReturnsUserNotifications() throws Exception {
        // Create some notifications
        Notification notif1 = new Notification();
        notif1.setUserId(100L);
        notif1.setMessage("Notification 1");
        notif1.setType("TYPE1");
        notif1.setIsRead(false);
        notificationRepository.save(notif1);

        Notification notif2 = new Notification();
        notif2.setUserId(100L);
        notif2.setMessage("Notification 2");
        notif2.setType("TYPE2");
        notif2.setIsRead(true);
        notificationRepository.save(notif2);

        mockMvc.perform(get("/notifications/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void getUnreadNotifications_ReturnsOnlyUnread() throws Exception {
        // Create notifications
        Notification unread = new Notification();
        unread.setUserId(100L);
        unread.setMessage("Unread notification");
        unread.setType("UNREAD");
        unread.setIsRead(false);
        notificationRepository.save(unread);

        Notification read = new Notification();
        read.setUserId(100L);
        read.setMessage("Read notification");
        read.setType("READ");
        read.setIsRead(true);
        notificationRepository.save(read);

        mockMvc.perform(get("/notifications/user/100/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].isRead").value(false));
    }

    @Test
    void getUnreadCount_ReturnsCount() throws Exception {
        // Create unread notifications
        for (int i = 0; i < 3; i++) {
            Notification notif = new Notification();
            notif.setUserId(100L);
            notif.setMessage("Unread " + i);
            notif.setType("UNREAD");
            notif.setIsRead(false);
            notificationRepository.save(notif);
        }

        mockMvc.perform(get("/notifications/user/100/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void markAsRead_Success() throws Exception {
        Notification notif = new Notification();
        notif.setUserId(100L);
        notif.setMessage("Test notification");
        notif.setType("TEST");
        notif.setIsRead(false);
        Notification saved = notificationRepository.save(notif);

        mockMvc.perform(patch("/notifications/" + saved.getId() + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isRead").value(true))
                .andExpect(jsonPath("$.data.readAt").exists());
    }

    @Test
    void markAsRead_NotFound() throws Exception {
        mockMvc.perform(patch("/notifications/invalid123/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Notification not found"));
    }

    @Test
    void markAllAsRead_Success() throws Exception {
        // Create unread notifications
        for (int i = 0; i < 3; i++) {
            Notification notif = new Notification();
            notif.setUserId(100L);
            notif.setMessage("Unread " + i);
            notif.setType("UNREAD");
            notif.setIsRead(false);
            notificationRepository.save(notif);
        }

        mockMvc.perform(patch("/notifications/user/100/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));
    }

    @Test
    void deleteNotification_Success() throws Exception {
        Notification notif = new Notification();
        notif.setUserId(100L);
        notif.setMessage("Test notification");
        notif.setType("TEST");
        notif.setIsRead(false);
        Notification saved = notificationRepository.save(notif);

        mockMvc.perform(delete("/notifications/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));
    }

    @Test
    void deleteAllUserNotifications_Success() throws Exception {
        // Create notifications for user 100
        for (int i = 0; i < 3; i++) {
            Notification notif = new Notification();
            notif.setUserId(100L);
            notif.setMessage("Notification " + i);
            notif.setType("TEST");
            notif.setIsRead(false);
            notificationRepository.save(notif);
        }

        mockMvc.perform(delete("/notifications/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All notifications deleted successfully"));
    }
}
