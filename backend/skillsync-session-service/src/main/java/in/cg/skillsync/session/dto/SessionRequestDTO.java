package in.cg.skillsync.session.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class SessionRequestDTO {

    @NotNull(message = "Mentor ID is required")
    private Long mentorId;

    @NotNull(message = "Session time is required")
    @Future(message = "Session time must be in the future")
    private LocalDateTime sessionTime;

    // Constructors
    public SessionRequestDTO() {}

    public SessionRequestDTO(Long mentorId, LocalDateTime sessionTime) {
        this.mentorId = mentorId;
        this.sessionTime = sessionTime;
    }

    // Getters & Setters
    public Long getMentorId() {
        return mentorId;
    }

    public void setMentorId(Long mentorId) {
        this.mentorId = mentorId;
    }

    public LocalDateTime getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(LocalDateTime sessionTime) {
        this.sessionTime = sessionTime;
    }
}