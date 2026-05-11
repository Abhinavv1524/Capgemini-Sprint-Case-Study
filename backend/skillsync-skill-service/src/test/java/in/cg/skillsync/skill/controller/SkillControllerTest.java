package in.cg.skillsync.skill.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.skill.config.SecurityConfig;
import in.cg.skillsync.skill.dto.SkillRequestDTO;
import in.cg.skillsync.skill.dto.SkillResponseDTO;
import in.cg.skillsync.skill.exception.GlobalExceptionHandler;
import in.cg.skillsync.skill.service.SkillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SkillController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SkillService skillService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void testCreateSkill_Success() throws Exception {
        SkillRequestDTO request = new SkillRequestDTO();
        request.setName("Spring Boot");

        when(skillService.createSkill(any(SkillRequestDTO.class), any()))
                .thenReturn(new SkillResponseDTO(1L, "Spring Boot", LocalDateTime.now()));

        mockMvc.perform(post("/api/skills")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testCreateSkill_InvalidRequest() throws Exception {
        SkillRequestDTO request = new SkillRequestDTO();

        mockMvc.perform(post("/api/skills")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSkillById_Unauthorized_NoHeaders() throws Exception {
        mockMvc.perform(get("/api/skills/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateSkill_Forbidden() throws Exception {
        SkillRequestDTO request = new SkillRequestDTO();
        request.setName("Java");

        when(skillService.createSkill(any(SkillRequestDTO.class), any()))
                .thenThrow(new UnauthorizedException("Only admin can create skills"));

        mockMvc.perform(post("/api/skills")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testGetAllSkills_Success() throws Exception {
        SkillResponseDTO skill = new SkillResponseDTO(1L, "Java", LocalDateTime.now());

        when(skillService.getAllSkills()).thenReturn(java.util.List.of(skill));

        mockMvc.perform(get("/api/skills")
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Java"));
    }
    
    @Test
    void testGetSkillById_Success() throws Exception {
        SkillResponseDTO response = new SkillResponseDTO(1L, "Spring Boot", LocalDateTime.now());

        when(skillService.getSkillById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/skills/1")
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Spring Boot"));
    }
    
    @Test
    void testGetAllSkills_Empty() throws Exception {
        when(skillService.getAllSkills()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/skills")
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
