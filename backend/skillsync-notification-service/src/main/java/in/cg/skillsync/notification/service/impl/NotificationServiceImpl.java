package in.cg.skillsync.notification.service.impl;

import in.cg.skillsync.notification.client.UserClient;
import in.cg.skillsync.notification.dto.ResponseDTO;
import in.cg.skillsync.notification.dto.UserDTO;
import in.cg.skillsync.notification.enums.NotificationType;
import in.cg.skillsync.notification.event.SessionEvent;
import in.cg.skillsync.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private UserClient userClient;
    private JavaMailSender mailSender;
    private String fromEmail;

    public NotificationServiceImpl() {
        this.fromEmail = "noreply@skillsync.local";
    }

    @Autowired
    public NotificationServiceImpl(UserClient userClient,
                                   JavaMailSender mailSender,
                                   @Value("${app.mail.from:noreply@skillsync.local}") String fromEmail) {
        this.userClient = userClient;
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void handleSessionEvent(SessionEvent event) {

        NotificationType type;

        try {
            type = NotificationType.valueOf(event.getEventType());
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Unknown event type received: {}", event.getEventType());
            return;
        }

        switch (type) {

            case SESSION_BOOKED -> notifyMentorForBookedSession(event);
            case SESSION_ACCEPTED -> notifyLearnerForAcceptedSession(event);
        }
    }

    private void notifyMentorForBookedSession(SessionEvent event) {
        UserDTO mentor = fetchUser(event.getMentorId());
        if (mentor == null || !StringUtils.hasText(mentor.getEmail())) {
            LOGGER.warn("Mentor details missing for authUserId={}, sessionId={}", event.getMentorId(), event.getSessionId());
            return;
        }

        String mentorName = StringUtils.hasText(mentor.getName()) ? mentor.getName() : "Mentor";
        String subject = "New session request #" + event.getSessionId();
        String body = "Hi " + mentorName + ",\n\n"
                + "A learner has booked a session with you.\n"
                + "Session ID: " + event.getSessionId() + "\n"
                + "Learner Auth User ID: " + event.getLearnerId() + "\n"
                + "Scheduled Time: " + event.getSessionTime() + "\n\n"
                + "Please review and confirm from your mentor dashboard.\n\n"
                + "Regards,\nSkillSync Team";

        sendEmail(mentor.getEmail(), subject, body, event.getSessionId(), NotificationType.SESSION_BOOKED);
    }

    private void notifyLearnerForAcceptedSession(SessionEvent event) {
        UserDTO learner = fetchUser(event.getLearnerId());
        if (learner == null || !StringUtils.hasText(learner.getEmail())) {
            LOGGER.warn("Learner details missing for authUserId={}, sessionId={}", event.getLearnerId(), event.getSessionId());
            return;
        }

        String learnerName = StringUtils.hasText(learner.getName()) ? learner.getName() : "Learner";
        String subject = "Session confirmed #" + event.getSessionId();
        String body = "Hi " + learnerName + ",\n\n"
                + "Your session request has been accepted by the mentor.\n"
                + "Session ID: " + event.getSessionId() + "\n"
                + "Mentor Auth User ID: " + event.getMentorId() + "\n"
                + "Scheduled Time: " + event.getSessionTime() + "\n\n"
                + "Thanks for using SkillSync.\n\n"
                + "Regards,\nSkillSync Team";

        sendEmail(learner.getEmail(), subject, body, event.getSessionId(), NotificationType.SESSION_ACCEPTED);
    }

    private UserDTO fetchUser(Long authUserId) {
        if (userClient == null) {
            LOGGER.warn("User client not initialized, skipping user lookup for authUserId={}", authUserId);
            return null;
        }
        try {
            ResponseDTO<UserDTO> response = userClient.getUserByAuthUserId(authUserId);
            return response != null ? response.getData() : null;
        } catch (Exception ex) {
            LOGGER.error("Failed to fetch user details from user-service for authUserId={}", authUserId, ex);
            return null;
        }
    }

    private void sendEmail(String to, String subject, String body, Long sessionId, NotificationType type) {
        if (mailSender == null) {
            LOGGER.warn("Mail sender not initialized, skipping email. type={}, sessionId={}, recipient={}", type, sessionId, to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            LOGGER.info("Notification email sent. type={}, sessionId={}, recipient={}", type, sessionId, to);
        } catch (Exception ex) {
            LOGGER.error("Failed to send notification email. type={}, sessionId={}, recipient={}", type, sessionId, to, ex);
        }
    }
}
