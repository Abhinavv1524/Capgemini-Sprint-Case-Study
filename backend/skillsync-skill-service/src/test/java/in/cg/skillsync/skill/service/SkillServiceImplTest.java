package in.cg.skillsync.skill.service;

import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.skill.dto.SkillRequestDTO;
import in.cg.skillsync.skill.dto.SkillResponseDTO;
import in.cg.skillsync.skill.entity.Skill;
import in.cg.skillsync.skill.repository.SkillRepository;
import in.cg.skillsync.skill.service.impl.SkillServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void testCreateSkill_Success() {
        SkillRequestDTO request = new SkillRequestDTO();
        request.setName("Spring Boot");
        HttpServletRequest servletRequest = requestWithRole("ROLE_ADMIN");

        Skill saved = new Skill(1L, "Spring Boot", LocalDateTime.now(), null);
        when(skillRepository.findByName("Spring Boot")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenReturn(saved);

        SkillResponseDTO response = skillService.createSkill(request, servletRequest);

        assertEquals(1L, response.getId());
        assertEquals("Spring Boot", response.getName());
    }

    @Test
    void testCreateSkill_Unauthorized() {
        SkillRequestDTO request = new SkillRequestDTO();
        request.setName("Spring Boot");

        assertThrows(UnauthorizedException.class,
                () -> skillService.createSkill(request, requestWithRole("ROLE_LEARNER")));
    }

    @Test
    void testCreateSkill_Duplicate() {
        SkillRequestDTO request = new SkillRequestDTO();
        request.setName("Java");
        HttpServletRequest servletRequest = requestWithRole("ROLE_ADMIN");

        when(skillRepository.findByName("Java")).thenReturn(Optional.of(new Skill()));

        assertThrows(BadRequestException.class, () -> skillService.createSkill(request, servletRequest));
    }

    @Test
    void testGetSkillById_NotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> skillService.getSkillById(99L));
    }

    @Test
    void testGetAllSkills_Success() {
        when(skillRepository.findAll()).thenReturn(List.of(
                new Skill(1L, "Java", LocalDateTime.now(), null),
                new Skill(2L, "Spring", LocalDateTime.now(), null)
        ));

        List<SkillResponseDTO> response = skillService.getAllSkills();
        assertEquals(2, response.size());
    }

    private HttpServletRequest requestWithRole(String role) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-Role")).thenReturn(role);
        return request;
    }
}
