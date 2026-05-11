package in.cg.skillsync.mentor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MentorRequestDTO {

    @NotBlank(message = "Bio is required")
    private String bio;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience must be >= 0")
    private Integer experience;

    @NotNull(message = "Hourly rate is required")
    @Min(value = 0, message = "Hourly rate must be >= 0")
    private Double hourlyRate;

    // Getters and Setters
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
}