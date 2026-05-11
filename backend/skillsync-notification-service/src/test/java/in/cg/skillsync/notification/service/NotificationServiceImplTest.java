package in.cg.skillsync.notification.service;

import in.cg.skillsync.notification.event.SessionEvent;
import in.cg.skillsync.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceImplTest {

    private final NotificationServiceImpl notificationService = new NotificationServiceImpl();

    @Test
    void testHandleSessionEvent_SessionBooked() {
        SessionEvent event = new SessionEvent("SESSION_BOOKED", 1L, 20L, 10L, LocalDateTime.now().plusDays(1));
        assertDoesNotThrow(() -> notificationService.handleSessionEvent(event));
    }

    @Test
    void testHandleSessionEvent_UnknownEventType() {
        SessionEvent event = new SessionEvent("SOMETHING_NEW", 1L, 20L, 10L, LocalDateTime.now().plusDays(1));
        assertDoesNotThrow(() -> notificationService.handleSessionEvent(event));
    }
    @Test
    void testHandleSessionEvent_SessionAccepted() {
        SessionEvent event = new SessionEvent(
                "SESSION_ACCEPTED",
                1L,
                20L,
                10L,
                LocalDateTime.now().plusDays(1)
        );

        assertDoesNotThrow(() -> notificationService.handleSessionEvent(event));
    }
}
