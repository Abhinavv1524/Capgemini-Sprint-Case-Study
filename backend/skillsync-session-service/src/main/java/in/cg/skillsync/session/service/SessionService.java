package in.cg.skillsync.session.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import in.cg.skillsync.session.dto.SessionRequestDTO;
import in.cg.skillsync.session.dto.SessionResponseDTO;

public interface SessionService {
    SessionResponseDTO createSession(SessionRequestDTO request, HttpServletRequest servletRequest);

    SessionResponseDTO acceptSession(Long sessionId, HttpServletRequest servletRequest);

    SessionResponseDTO rejectSession(Long sessionId, HttpServletRequest servletRequest);

    SessionResponseDTO cancelSession(Long sessionId, HttpServletRequest servletRequest);

    List<SessionResponseDTO> getSessionsByUser(HttpServletRequest servletRequest);
    
    SessionResponseDTO getSessionById(Long id);
}

