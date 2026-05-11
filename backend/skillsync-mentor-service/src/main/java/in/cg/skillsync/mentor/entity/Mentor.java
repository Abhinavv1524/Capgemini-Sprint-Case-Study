package in.cg.skillsync.mentor.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mentors")
public class Mentor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	private Long userId;

    @Column(length = 1000)
    private String bio;

    private Integer experience;

    private Double hourlyRate;

    @Enumerated(EnumType.STRING)
    private MentorStatus status;

    private LocalDateTime createdAt;
    
    // Constructors
    public Mentor() {}

    public Mentor(Long userId, String bio, Integer experience, Double hourlyRate, MentorStatus status, LocalDateTime createdAt) {
        this.userId = userId;
        this.bio = bio;
        this.experience = experience;
        this.hourlyRate = hourlyRate;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public MentorStatus getStatus() {
        return status;
    }

    public void setStatus(MentorStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
