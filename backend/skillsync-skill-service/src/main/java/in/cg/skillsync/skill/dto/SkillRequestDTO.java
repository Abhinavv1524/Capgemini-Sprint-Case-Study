package in.cg.skillsync.skill.dto;

import jakarta.validation.constraints.NotBlank;

public class SkillRequestDTO {

    @NotBlank(message = "Skill name is required")
    private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
}