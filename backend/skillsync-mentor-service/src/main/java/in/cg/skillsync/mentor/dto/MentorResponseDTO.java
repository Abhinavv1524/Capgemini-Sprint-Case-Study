package in.cg.skillsync.mentor.dto;

import in.cg.skillsync.mentor.entity.MentorStatus;

public class MentorResponseDTO {

    private Long id;
    private Long userId;
    private String bio;
    private Integer experience;
    private Double hourlyRate;
    private MentorStatus status;

    public MentorResponseDTO() {}

    public MentorResponseDTO(Long id, Long userId, String bio, Integer experience, Double hourlyRate, MentorStatus status) {
        this.id = id;
        this.userId = userId;
        this.bio = bio;
        this.experience = experience;
        this.hourlyRate = hourlyRate;
        this.status = status;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getBio() {
        return bio;
    }

    public Integer getExperience() {
        return experience;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public MentorStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setStatus(MentorStatus status) {
        this.status = status;
    }
}