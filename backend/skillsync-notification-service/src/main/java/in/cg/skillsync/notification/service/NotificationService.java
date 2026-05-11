package in.cg.skillsync.notification.service;

import in.cg.skillsync.notification.event.SessionEvent;

public interface NotificationService {
    void handleSessionEvent(SessionEvent event);
}