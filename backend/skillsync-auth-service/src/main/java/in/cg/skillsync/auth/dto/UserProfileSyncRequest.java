package in.cg.skillsync.auth.dto;

public class UserProfileSyncRequest {

    private String name;
    private String email;
    private String bio;
    private String skills;

    public UserProfileSyncRequest() {
    }

    public UserProfileSyncRequest(String name, String email, String bio, String skills) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.skills = skills;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    public String getSkills() {
        return skills;
    }
}
