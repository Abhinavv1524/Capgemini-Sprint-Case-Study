package in.cg.skillsync.session.controller;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.session.dto.SessionRequestDTO;
import in.cg.skillsync.session.dto.SessionResponseDTO;
import in.cg.skillsync.session.service.SessionService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // Create Session (Learner)
    @PostMapping
    public ResponseEntity<ResponseDTO<SessionResponseDTO>> createSession(
            @Valid @RequestBody SessionRequestDTO request,
            HttpServletRequest servletRequest) {

        SessionResponseDTO response = sessionService.createSession(request, servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Session created successfully", response)
        );
    }

    // Accept Session (Mentor)
    @PutMapping("/{id}/accept")
    public ResponseEntity<ResponseDTO<SessionResponseDTO>> acceptSession(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {

        SessionResponseDTO response = sessionService.acceptSession(id, servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Session accepted successfully", response)
        );
    }

    // Reject Session (Mentor)
    @PutMapping("/{id}/reject")
    public ResponseEntity<ResponseDTO<SessionResponseDTO>> rejectSession(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {

        SessionResponseDTO response = sessionService.rejectSession(id, servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Session rejected successfully", response)
        );
    }

    // Cancel Session (Learner)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ResponseDTO<SessionResponseDTO>> cancelSession(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {

        SessionResponseDTO response = sessionService.cancelSession(id, servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Session cancelled successfully", response)
        );
    }

    // Get Sessions (Both)
    @GetMapping("/user")
    public ResponseEntity<ResponseDTO<List<SessionResponseDTO>>> getSessions(HttpServletRequest servletRequest) {

        List<SessionResponseDTO> sessions = sessionService.getSessionsByUser(servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Sessions fetched successfully", sessions)
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<SessionResponseDTO>> getSessionById(
            @PathVariable Long id) {

        SessionResponseDTO session = sessionService.getSessionById(id);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Session fetched successfully", session)
        );
    }
}
