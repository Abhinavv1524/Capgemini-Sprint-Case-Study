package in.cg.skillsync.notification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.cg.skillsync.notification.event.SessionEvent;
import in.cg.skillsync.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notifications/test")
public class TestController {

    private final NotificationService notificationService;

    public TestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public String test(@RequestBody SessionEvent event) {
        notificationService.handleSessionEvent(event);
        return "Event processed";
    }
}
