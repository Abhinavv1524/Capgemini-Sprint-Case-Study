package in.cg.skillsync.skill.service;

import in.cg.skillsync.skill.dto.SkillRequestDTO;
import in.cg.skillsync.skill.dto.SkillResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SkillService {

    SkillResponseDTO createSkill(SkillRequestDTO requestDTO, HttpServletRequest servletRequest);

    List<SkillResponseDTO> getAllSkills();

    SkillResponseDTO getSkillById(Long id);
}
