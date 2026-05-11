package in.cg.skillsync.group.dto;

import java.time.LocalDateTime;

public class GroupResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;

    public GroupResponseDTO() {}

    public GroupResponseDTO(Long id, String name, String description, Long createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    // Getters & Setters
    
    
}