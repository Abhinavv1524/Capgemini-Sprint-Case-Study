package in.cg.skillsync.mentor.controller;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.mentor.dto.MentorRequestDTO;
import in.cg.skillsync.mentor.dto.MentorResponseDTO;
import in.cg.skillsync.mentor.service.MentorService;
import in.cg.skillsync.mentor.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentors")
public class MentorController {

    private final MentorService mentorService;

    public MentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    // Apply as mentor
    @PostMapping("/apply")
    public ResponseEntity<ResponseDTO<MentorResponseDTO>> applyForMentor(
            HttpServletRequest servletRequest,
            @Valid @RequestBody MentorRequestDTO requestDTO) {

        Long userId = getRequiredUserId(servletRequest);
        String role = getRequiredRole(servletRequest);
        MentorResponseDTO response = mentorService.applyForMentor(userId, role, requestDTO);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Mentor application submitted successfully", response)
        );
    }

    // Admin verification
    @PutMapping("/{id}/approve")
    public ResponseEntity<ResponseDTO<MentorResponseDTO>> approveMentor(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {

        String role = getRequiredRole(servletRequest);
        String authorizationHeader = servletRequest.getHeader("Authorization");
        MentorResponseDTO response = mentorService.approveMentor(id, role, authorizationHeader);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Mentor application approved successfully", response)
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ResponseDTO<MentorResponseDTO>> rejectMentor(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {

        String role = getRequiredRole(servletRequest);
        MentorResponseDTO response = mentorService.rejectMentor(id, role);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Mentor application rejected successfully", response)
        );
    }

    // Get mentor by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<MentorResponseDTO>> getMentorById(@PathVariable Long id) {

        MentorResponseDTO response = mentorService.getMentorById(id);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Mentor fetched successfully", response)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseDTO<MentorResponseDTO>> getMentorByUserId(@PathVariable Long userId) {

        MentorResponseDTO response = mentorService.getMentorByUserId(userId);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Mentor fetched successfully", response)
        );
    }

    // Get all mentors
    @GetMapping
    public ResponseEntity<ResponseDTO<List<MentorResponseDTO>>> getAllMentors() {

        List<MentorResponseDTO> response = mentorService.getAllMentors();

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Mentors fetched successfully", response)
        );
    }

    // Update mentor
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<MentorResponseDTO>> updateMentor(
            @PathVariable Long id,
            HttpServletRequest servletRequest,
            @Valid @RequestBody MentorRequestDTO requestDTO) {

        Long userId = getRequiredUserId(servletRequest);
        MentorResponseDTO response = mentorService.updateMentor(id, userId, requestDTO);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Mentor updated successfully", response)
        );
    }

    private Long getRequiredUserId(HttpServletRequest request) {
        String userId = SecurityUtil.getCurrentUserId(request);
        try {
            return Long.parseLong(userId);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid user context");
        }
    }

    private String getRequiredRole(HttpServletRequest request) {
        String role = SecurityUtil.getCurrentUserRole(request);
        if (role == null || role.isBlank()) {
            throw new UnauthorizedException("Invalid role context");
        }
        return role;
    }
}
