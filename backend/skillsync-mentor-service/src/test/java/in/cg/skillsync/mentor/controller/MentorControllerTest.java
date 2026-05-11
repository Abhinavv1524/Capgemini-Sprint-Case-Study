package in.cg.skillsync.mentor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.mentor.config.SecurityConfig;
import in.cg.skillsync.mentor.dto.MentorRequestDTO;
import in.cg.skillsync.mentor.dto.MentorResponseDTO;
import in.cg.skillsync.mentor.entity.MentorStatus;
import in.cg.skillsync.mentor.exception.GlobalExceptionHandler;
import in.cg.skillsync.mentor.service.MentorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MentorController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MentorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MentorService mentorService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void testApplyForMentor_Success() throws Exception {
        MentorRequestDTO request = buildRequest();
        MentorResponseDTO response = new MentorResponseDTO(1L, 10L, "bio", 5, 500.0, MentorStatus.PENDING);

        when(mentorService.applyForMentor(eq(10L), eq("ROLE_LEARNER"), any(MentorRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/mentors/apply")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testApplyForMentor_InvalidBody() throws Exception {
        MentorRequestDTO request = new MentorRequestDTO();
        request.setExperience(5);
        request.setHourlyRate(500.0);

        mockMvc.perform(post("/api/mentors/apply")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMentorById_Unauthorized_NoHeaders() throws Exception {
        mockMvc.perform(get("/api/mentors/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateMentor_Forbidden() throws Exception {
        MentorRequestDTO request = buildRequest();

        when(mentorService.updateMentor(eq(1L), eq(20L), any(MentorRequestDTO.class)))
                .thenThrow(new UnauthorizedException("Only mentor can update own profile"));

        mockMvc.perform(put("/api/mentors/1")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "20")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    private MentorRequestDTO buildRequest() {
        MentorRequestDTO request = new MentorRequestDTO();
        request.setBio("Java mentor");
        request.setExperience(5);
        request.setHourlyRate(500.0);
        return request;
    }
}
