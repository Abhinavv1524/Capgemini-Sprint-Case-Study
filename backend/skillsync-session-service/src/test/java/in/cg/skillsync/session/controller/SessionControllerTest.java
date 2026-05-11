package in.cg.skillsync.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.session.config.SecurityConfig;
import in.cg.skillsync.session.dto.SessionRequestDTO;
import in.cg.skillsync.session.dto.SessionResponseDTO;
import in.cg.skillsync.session.enums.SessionStatus;
import in.cg.skillsync.session.exception.GlobalExceptionHandler;
import in.cg.skillsync.session.service.SessionService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void testCreateSession_Success() throws Exception {
        SessionRequestDTO request = new SessionRequestDTO(200L, LocalDateTime.now().plusDays(1));
        SessionResponseDTO response = new SessionResponseDTO(1L, 200L, 100L, request.getSessionTime(), SessionStatus.REQUESTED, LocalDateTime.now());

        when(sessionService.createSession(any(SessionRequestDTO.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "100")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testCreateSession_InvalidBody() throws Exception {
        SessionRequestDTO request = new SessionRequestDTO();

        mockMvc.perform(post("/api/sessions")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "100")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAcceptSession_Unauthorized_NoHeaders() throws Exception {
        mockMvc.perform(put("/api/sessions/1/accept"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAcceptSession_Forbidden() throws Exception {
        when(sessionService.acceptSession(eq(1L), any()))
                .thenThrow(new UnauthorizedException("Only mentors can accept sessions"));

        mockMvc.perform(put("/api/sessions/1/accept")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "100")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetSessionById_Success() throws Exception {
        SessionResponseDTO response = new SessionResponseDTO(1L, 200L, 100L, LocalDateTime.now().plusDays(1), SessionStatus.REQUESTED, LocalDateTime.now());

        when(sessionService.getSessionById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/1")
                        .with(user("mentor").roles("MENTOR"))
                        .header("X-User-Id", "200")
                        .header("X-User-Role", "ROLE_MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
    
    @Test
    void testAcceptSession_Success() throws Exception {
        SessionResponseDTO response = new SessionResponseDTO(1L, 200L, 100L,
                LocalDateTime.now().plusDays(1), SessionStatus.ACCEPTED, LocalDateTime.now());

        when(sessionService.acceptSession(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/sessions/1/accept")
                        .with(csrf())
                        .with(user("mentor").roles("MENTOR"))
                        .header("X-User-Id", "200")
                        .header("X-User-Role", "ROLE_MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }
    
    @Test
    void testRejectSession_Success() throws Exception {
        SessionResponseDTO response = new SessionResponseDTO(1L, 200L, 100L,
                LocalDateTime.now(), SessionStatus.REJECTED, LocalDateTime.now());

        when(sessionService.rejectSession(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/sessions/1/reject")
                        .with(csrf())
                        .with(user("mentor").roles("MENTOR"))
                        .header("X-User-Id", "200")
                        .header("X-User-Role", "ROLE_MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }
    
    @Test
    void testCancelSession_Success() throws Exception {
        SessionResponseDTO response = new SessionResponseDTO(1L, 200L, 100L,
                LocalDateTime.now(), SessionStatus.CANCELLED, LocalDateTime.now());

        when(sessionService.cancelSession(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/sessions/1/cancel")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "100")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
    
    @Test
    void testGetSessionsByUser_Success() throws Exception {
        SessionResponseDTO response = new SessionResponseDTO(1L, 200L, 100L,
                LocalDateTime.now(), SessionStatus.REQUESTED, LocalDateTime.now());

        when(sessionService.getSessionsByUser(any())).thenReturn(java.util.List.of(response));

        mockMvc.perform(get("/api/sessions/user")
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "100")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }
}
