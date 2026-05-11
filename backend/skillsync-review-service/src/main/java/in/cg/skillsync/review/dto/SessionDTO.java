package in.cg.skillsync.review.dto;

public class SessionDTO {

    private Long id;
    private Long mentorId;
    private Long learnerId;
    private String status;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
    
}