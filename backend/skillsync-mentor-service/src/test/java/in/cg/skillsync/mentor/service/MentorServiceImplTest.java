package in.cg.skillsync.mentor.service;

import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.mentor.dto.MentorRequestDTO;
import in.cg.skillsync.mentor.dto.MentorResponseDTO;
import in.cg.skillsync.mentor.entity.Mentor;
import in.cg.skillsync.mentor.entity.MentorStatus;
import in.cg.skillsync.mentor.repository.MentorRepository;
import in.cg.skillsync.mentor.service.impl.MentorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentorServiceImplTest {

    @Mock
    private MentorRepository mentorRepository;

    @InjectMocks
    private MentorServiceImpl mentorService;

    @Test
    void testApplyForMentor_Success() {
        MentorRequestDTO request = buildRequest();
        Mentor saved = buildMentor(1L, 100L, MentorStatus.PENDING);

        when(mentorRepository.findByUserId(100L)).thenReturn(Optional.empty());
        when(mentorRepository.save(any(Mentor.class))).thenReturn(saved);

        MentorResponseDTO response = mentorService.applyForMentor(100L, "ROLE_LEARNER", request);

        assertEquals(1L, response.getId());
        assertEquals(MentorStatus.PENDING, response.getStatus());
        assertEquals(100L, response.getUserId());
    }

    @Test
    void testApplyForMentor_AlreadyApplied() {
        when(mentorRepository.findByUserId(100L)).thenReturn(Optional.of(new Mentor()));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> mentorService.applyForMentor(100L, "ROLE_LEARNER", buildRequest()));

        assertEquals("User has already applied as mentor", ex.getMessage());
    }

    @Test
    void testApplyForMentor_ForbiddenRole() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> mentorService.applyForMentor(100L, "ROLE_MENTOR", buildRequest()));

        assertEquals("Only learners can apply as mentor", ex.getMessage());
    }

    @Test
    void testGetMentorById_NotFound() {
        when(mentorRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> mentorService.getMentorById(999L));
    }

    @Test
    void testGetAllMentors_Success() {
        when(mentorRepository.findAll()).thenReturn(List.of(
                buildMentor(1L, 100L, MentorStatus.PENDING),
                buildMentor(2L, 200L, MentorStatus.APPROVED)
        ));

        List<MentorResponseDTO> response = mentorService.getAllMentors();
        assertEquals(2, response.size());
    }

    @Test
    void testUpdateMentor_Success() {
        Mentor existing = buildMentor(1L, 100L, MentorStatus.PENDING);
        Mentor updated = buildMentor(1L, 100L, MentorStatus.PENDING);
        updated.setBio("Updated bio");

        MentorRequestDTO request = buildRequest();
        request.setBio("Updated bio");

        when(mentorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(updated);

        MentorResponseDTO response = mentorService.updateMentor(1L, 100L, request);
        assertEquals("Updated bio", response.getBio());
    }

    @Test
    void testUpdateMentor_NotOwner() {
        Mentor existing = buildMentor(1L, 100L, MentorStatus.PENDING);

        when(mentorRepository.findById(1L)).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> mentorService.updateMentor(1L, 999L, buildRequest()));
        assertEquals("You are not allowed to update this mentor profile", ex.getMessage());
    }

    private MentorRequestDTO buildRequest() {
        MentorRequestDTO request = new MentorRequestDTO();
        request.setBio("Java Mentor");
        request.setExperience(5);
        request.setHourlyRate(500.0);
        return request;
    }

    private Mentor buildMentor(Long id, Long userId, MentorStatus status) {
        Mentor mentor = new Mentor();
        ReflectionTestUtils.setField(mentor, "id", id);
        mentor.setUserId(userId);
        mentor.setBio("bio");
        mentor.setExperience(5);
        mentor.setHourlyRate(500.0);
        mentor.setStatus(status);
        return mentor;
    }
}
