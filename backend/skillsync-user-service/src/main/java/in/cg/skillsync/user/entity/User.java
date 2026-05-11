package in.cg.skillsync.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authUserId;   // from Auth Service

    private String name;

    private String email;

    private String bio;

    private String skills; // comma-separated for now

    private String role; // ROLE_LEARNER / ROLE_MENTOR

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public User() {}

	public User(Long id, Long authUserId, String name, String email, String bio, String skills, String role,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.authUserId = authUserId;
		this.name = name;
		this.email = email;
		this.bio = bio;
		this.skills = skills;
		this.role = role;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAuthUserId() {
		return authUserId;
	}

	public void setAuthUserId(Long authUserId) {
		this.authUserId = authUserId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	@PrePersist
	public void prePersist() {
	    this.createdAt = LocalDateTime.now();
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	@PreUpdate
	public void preUpdate() {
	    this.updatedAt = LocalDateTime.now();
	}
    
    
    
}