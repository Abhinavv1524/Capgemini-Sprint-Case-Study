package in.cg.skillsync.user.dto;

public class UserResponseDTO {

    private Long id;
    private Long authUserId;
    private String name;
    private String email;
    private String bio;
    private String skills;
    private String role;
    
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
    
}