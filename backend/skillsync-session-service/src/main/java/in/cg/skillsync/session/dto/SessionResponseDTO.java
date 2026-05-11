package in.cg.skillsync.session.dto;

import in.cg.skillsync.session.enums.SessionStatus;

import java.time.LocalDateTime;

public class SessionResponseDTO {

    private Long id;
    private Long mentorId;
    private Long learnerId;
    private LocalDateTime sessionTime;
    private SessionStatus status;
    private LocalDateTime createdAt;

    // Constructors
    public SessionResponseDTO() {}

    public SessionResponseDTO(Long id, Long mentorId, Long learnerId,
                              LocalDateTime sessionTime, SessionStatus status,
                              LocalDateTime createdAt) {
        this.id = id;
        this.mentorId = mentorId;
        this.learnerId = learnerId;
        this.sessionTime = sessionTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters & Setters

    public Long getId() {
        return id;
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

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}