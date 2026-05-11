package in.cg.skillsync.session.event;

import in.cg.skillsync.session.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SessionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(SessionEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SESSION_QUEUE,
                event
        );

        System.out.println("Event published: " + event.getEventType());
    }
}