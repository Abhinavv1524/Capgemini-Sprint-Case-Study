package in.cg.skillsync.group.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateGroupRequestDTO {

    @NotBlank(message = "Group name is required")
    private String name;

    private String description;

    public CreateGroupRequestDTO() {}

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
}