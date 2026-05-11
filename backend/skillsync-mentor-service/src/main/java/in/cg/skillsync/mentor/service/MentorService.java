package in.cg.skillsync.mentor.service;

import in.cg.skillsync.mentor.dto.MentorRequestDTO;
import in.cg.skillsync.mentor.dto.MentorResponseDTO;

import java.util.List;

public interface MentorService {

    MentorResponseDTO applyForMentor(Long userId, String role, MentorRequestDTO requestDTO);

    MentorResponseDTO approveMentor(Long id, String role, String authorizationHeader);

    MentorResponseDTO rejectMentor(Long id, String role);

    MentorResponseDTO getMentorById(Long id);

    MentorResponseDTO getMentorByUserId(Long userId);

    List<MentorResponseDTO> getAllMentors();
    
    MentorResponseDTO updateMentor(Long id, Long userId, MentorRequestDTO requestDTO);
}
