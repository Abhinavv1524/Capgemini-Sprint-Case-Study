package in.cg.skillsync.skill.service.impl;

import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.skill.dto.SkillRequestDTO;
import in.cg.skillsync.skill.dto.SkillResponseDTO;
import in.cg.skillsync.skill.entity.Skill;
import in.cg.skillsync.skill.repository.SkillRepository;
import in.cg.skillsync.skill.service.SkillService;
import in.cg.skillsync.skill.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    public SkillServiceImpl(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    @Caching(
            put = @CachePut(value = "skillsById", key = "#result.id"),
            evict = @CacheEvict(value = "allSkills", allEntries = true)
    )
    public SkillResponseDTO createSkill(SkillRequestDTO requestDTO, HttpServletRequest servletRequest) {
        String role = SecurityUtil.getCurrentUserRole(servletRequest);

        // Role check
        if (!"ROLE_ADMIN".equals(role)) {
            throw new UnauthorizedException("Only admin can create skills");
        }

        // Duplicate check
        skillRepository.findByName(requestDTO.getName())
                .ifPresent(skill -> {
                    throw new BadRequestException("Skill already exists");
                });

        // Create skill
        Skill skill = new Skill();
        skill.setName(requestDTO.getName());

        Skill savedSkill = skillRepository.save(skill);

        return mapToDTO(savedSkill);
    }

    @Override
    @Cacheable(value = "allSkills")
    public List<SkillResponseDTO> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "skillsById", key = "#id")
    public SkillResponseDTO getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        return mapToDTO(skill);
    }

    private SkillResponseDTO mapToDTO(Skill skill) {
        return new SkillResponseDTO(
                skill.getId(),
                skill.getName(),
                skill.getCreatedAt()
        );
    }
}
