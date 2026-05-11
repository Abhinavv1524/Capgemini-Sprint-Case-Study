package in.cg.skillsync.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.skillsync.notification.config.SecurityConfig;
import in.cg.skillsync.notification.event.SessionEvent;
import in.cg.skillsync.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@Import({SecurityConfig.class})
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CacheManager cacheManager;

    @Test
    void testTestEndpoint_Unauthorized_NoHeaders() throws Exception {
        SessionEvent event = new SessionEvent("SESSION_BOOKED", 1L, 20L, 10L, LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/notifications/test")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testTestEndpoint_Success() throws Exception {
        SessionEvent event = new SessionEvent("SESSION_BOOKED", 1L, 20L, 10L, LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/notifications/test")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(content().string("Event processed"));
    }
}
