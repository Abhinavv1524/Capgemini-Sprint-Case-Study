package in.cg.skillsync.notification.event;

import in.cg.skillsync.notification.config.RabbitMQConfig;
import in.cg.skillsync.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SessionEventListener {

    private final NotificationService notificationService;

    public SessionEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.SESSION_QUEUE)
    public void consumeSessionEvent(SessionEvent event) {
        System.out.println("Event received: " + event.getEventType());
        notificationService.handleSessionEvent(event);
    }
}