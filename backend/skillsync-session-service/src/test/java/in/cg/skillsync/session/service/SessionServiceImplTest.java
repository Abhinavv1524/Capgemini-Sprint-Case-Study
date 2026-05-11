package in.cg.skillsync.session.service;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.session.client.MentorClient;
import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.session.client.UserClient;
import in.cg.skillsync.session.dto.MentorDTO;
import in.cg.skillsync.session.dto.SessionRequestDTO;
import in.cg.skillsync.session.dto.SessionResponseDTO;
import in.cg.skillsync.session.dto.UserDTO;
import in.cg.skillsync.session.entity.Session;
import in.cg.skillsync.session.enums.SessionStatus;
import in.cg.skillsync.session.event.SessionEventPublisher;
import in.cg.skillsync.session.repository.SessionRepository;
import in.cg.skillsync.session.service.impl.SessionServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionEventPublisher eventPublisher;

    @Mock
    private UserClient userClient;

    @Mock
    private MentorClient mentorClient;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void testCreateSession_Success() {
        SessionRequestDTO request = new SessionRequestDTO(200L, LocalDateTime.now().plusDays(1));
        HttpServletRequest servletRequest = requestWithHeaders("100", "ROLE_LEARNER");

        UserDTO learner = userDto(100L, "ROLE_LEARNER");
        MentorDTO mentor = mentorDto(200L, 200L, "APPROVED");

        when(userClient.getUserByAuthUserId(100L)).thenReturn(new ResponseDTO<>(true, "ok", learner));
        when(mentorClient.getMentorById(200L)).thenReturn(new ResponseDTO<>(true, "ok", mentor));

        Session saved = new Session(200L, 100L, request.getSessionTime(), SessionStatus.REQUESTED);
        ReflectionTestUtils.setField(saved, "id", 1L);

        when(sessionRepository.save(any(Session.class))).thenReturn(saved);

        SessionResponseDTO response = sessionService.createSession(request, servletRequest);

        assertEquals(1L, response.getId());
        assertEquals(200L, response.getMentorId());
        assertEquals(100L, response.getLearnerId());
        assertEquals(SessionStatus.REQUESTED, response.getStatus());
        verify(eventPublisher).publish(any());
    }

    @Test
    void testCreateSession_ForbiddenRole() {
        SessionRequestDTO request = new SessionRequestDTO(200L, LocalDateTime.now().plusDays(1));
        HttpServletRequest servletRequest = requestWithHeaders("100", "ROLE_MENTOR");

        assertThrows(UnauthorizedException.class, () -> sessionService.createSession(request, servletRequest));
    }

    @Test
    void testAcceptSession_Success() {
        HttpServletRequest servletRequest = requestWithHeaders("200", "ROLE_MENTOR");

        Session session = new Session(200L, 100L, LocalDateTime.now().plusDays(1), SessionStatus.REQUESTED);
        ReflectionTestUtils.setField(session, "id", 1L);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponseDTO response = sessionService.acceptSession(1L, servletRequest);

        assertEquals(SessionStatus.ACCEPTED, response.getStatus());
        verify(eventPublisher).publish(any());
    }

    @Test
    void testAcceptSession_NotOwner() {
        HttpServletRequest servletRequest = requestWithHeaders("200", "ROLE_MENTOR");

        Session session = new Session(300L, 100L, LocalDateTime.now().plusDays(1), SessionStatus.REQUESTED);
        ReflectionTestUtils.setField(session, "id", 1L);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> sessionService.acceptSession(1L, servletRequest));
        assertEquals("You are not allowed to perform this action", ex.getMessage());
    }

    @Test
    void testGetSessionById_NotFound() {
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sessionService.getSessionById(999L));
    }
    
    @Test
    void testCreateSession_LearnerNull() {
        SessionRequestDTO request = new SessionRequestDTO(200L, LocalDateTime.now());
        HttpServletRequest servletRequest = requestWithHeaders("100", "ROLE_LEARNER");

        when(userClient.getUserByAuthUserId(100L))
                .thenReturn(new ResponseDTO<>(true, "ok", null));

        assertThrows(BadRequestException.class,
                () -> sessionService.createSession(request, servletRequest));
    }
    
    @Test
    void testCreateSession_MentorNotFound() {
        SessionRequestDTO request = new SessionRequestDTO(200L, LocalDateTime.now());
        HttpServletRequest servletRequest = requestWithHeaders("100", "ROLE_LEARNER");

        when(userClient.getUserByAuthUserId(100L))
                .thenReturn(new ResponseDTO<>(true, "ok", userDto(100L, "ROLE_LEARNER")));

        when(userClient.getUserByAuthUserId(200L))
                .thenReturn(new ResponseDTO<>(true, "ok", null));

        assertThrows(BadRequestException.class,
                () -> sessionService.createSession(request, servletRequest));
    }
    
    @Test
    void testCreateSession_InvalidMentorRole() {
        SessionRequestDTO request = new SessionRequestDTO(200L, LocalDateTime.now());
        HttpServletRequest servletRequest = requestWithHeaders("100", "ROLE_LEARNER");

        when(userClient.getUserByAuthUserId(100L))
                .thenReturn(new ResponseDTO<>(true, "ok", userDto(100L, "ROLE_LEARNER")));

        when(userClient.getUserByAuthUserId(200L))
                .thenReturn(new ResponseDTO<>(true, "ok", userDto(200L, "ROLE_LEARNER")));

        assertThrows(BadRequestException.class,
                () -> sessionService.createSession(request, servletRequest));
    }
    
    @Test
    void testAcceptSession_InvalidState() {
        HttpServletRequest servletRequest = requestWithHeaders("200", "ROLE_MENTOR");

        Session session = new Session(200L, 100L, LocalDateTime.now(), SessionStatus.ACCEPTED);
        ReflectionTestUtils.setField(session, "id", 1L);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThrows(BadRequestException.class,
                () -> sessionService.acceptSession(1L, servletRequest));
    }
    
    @Test
    void testCancelSession_InvalidState() {
        HttpServletRequest servletRequest = requestWithHeaders("100", "ROLE_LEARNER");

        Session session = new Session(200L, 100L, LocalDateTime.now(), SessionStatus.COMPLETED);
        ReflectionTestUtils.setField(session, "id", 1L);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThrows(BadRequestException.class,
                () -> sessionService.cancelSession(1L, servletRequest));
    }

    private HttpServletRequest requestWithHeaders(String userId, String role) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        lenient().when(request.getHeader("X-User-Id")).thenReturn(userId);
        lenient().when(request.getHeader("X-User-Role")).thenReturn(role);
        return request;
    }

    private UserDTO userDto(Long id, String role) {
        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setRole(role);
        dto.setName("User");
        dto.setEmail("u@test.com");
        return dto;
    }

    private MentorDTO mentorDto(Long id, Long userId, String status) {
        MentorDTO dto = new MentorDTO();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setStatus(status);
        return dto;
    }
}
