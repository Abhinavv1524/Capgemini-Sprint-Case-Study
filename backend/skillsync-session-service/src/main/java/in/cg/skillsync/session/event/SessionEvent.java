package in.cg.skillsync.session.event;

import java.time.LocalDateTime;

public class SessionEvent {

    private String eventType;
    private Long sessionId;
    private Long mentorId;
    private Long learnerId;
    private LocalDateTime sessionTime;

    public SessionEvent() {}

    public SessionEvent(String eventType, Long sessionId, Long mentorId,
                        Long learnerId, LocalDateTime sessionTime) {
        this.eventType = eventType;
        this.sessionId = sessionId;
        this.mentorId = mentorId;
        this.learnerId = learnerId;
        this.sessionTime = sessionTime;
    }

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}

	public Long getMentorId() {
		return mentorId;
	}

	public void setMentorId(Long mentorId) {
		this.mentorId = mentorId;
	}

	public Long getLearnerId() {
		return learnerId;
	}

	public void setLearnerId(Long learnerId) {
		this.learnerId = learnerId;
	}

	public LocalDateTime getSessionTime() {
		return sessionTime;
	}

	public void setSessionTime(LocalDateTime sessionTime) {
		this.sessionTime = sessionTime;
	}

}