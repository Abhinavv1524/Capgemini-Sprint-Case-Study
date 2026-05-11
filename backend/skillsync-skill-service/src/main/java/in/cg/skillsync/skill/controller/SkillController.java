package in.cg.skillsync.skill.controller;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.skill.dto.SkillRequestDTO;
import in.cg.skillsync.skill.dto.SkillResponseDTO;
import in.cg.skillsync.skill.service.SkillService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    // CREATE SKILL
    @PostMapping
    public ResponseEntity<ResponseDTO<SkillResponseDTO>> createSkill(
            @Valid @RequestBody SkillRequestDTO requestDTO,
            HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(
                new ResponseDTO<>(
                        true,
                        "Skill created successfully",
                        skillService.createSkill(requestDTO, servletRequest)
                )
        );
    }

    // GET ALL SKILLS
    @GetMapping
    public ResponseEntity<ResponseDTO<List<SkillResponseDTO>>> getAllSkills() {
        return ResponseEntity.ok(
                new ResponseDTO<>(
                        true,
                        "Skills fetched successfully",
                        skillService.getAllSkills()
                )
        );
    }

    // GET SKILL BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<SkillResponseDTO>> getSkillById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                new ResponseDTO<>(
                        true,
                        "Skill fetched successfully",
                        skillService.getSkillById(id)
                )
        );
    }
}
