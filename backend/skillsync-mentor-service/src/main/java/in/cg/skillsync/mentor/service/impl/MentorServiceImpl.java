package in.cg.skillsync.mentor.service.impl;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.ResourceNotFoundException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.mentor.client.AuthClient;
import in.cg.skillsync.mentor.dto.MentorRequestDTO;
import in.cg.skillsync.mentor.dto.MentorResponseDTO;
import in.cg.skillsync.mentor.entity.Mentor;
import in.cg.skillsync.mentor.entity.MentorStatus;
import in.cg.skillsync.mentor.repository.MentorRepository;
import in.cg.skillsync.mentor.service.MentorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorServiceImpl implements MentorService {
 	@Autowired
    private MentorRepository mentorRepository;

    @Autowired
    private AuthClient authClient;

    @Override
    @Transactional
    @Caching(
            put = @CachePut(value = "mentorsById", key = "#result.id"),
            evict = @CacheEvict(value = "allMentors", allEntries = true)
    )
    public MentorResponseDTO applyForMentor(Long userId, String role, MentorRequestDTO requestDTO) {

        if (!"ROLE_LEARNER".equals(role)) {
            throw new UnauthorizedException("Only learners can apply as mentor");
        }

        mentorRepository.findByUserId(userId).ifPresent(m -> {
            throw new BadRequestException("User has already applied as mentor");
        });

        Mentor mentor = new Mentor();
        mentor.setUserId(userId);
        mentor.setBio(requestDTO.getBio());
        mentor.setExperience(requestDTO.getExperience());
        mentor.setHourlyRate(requestDTO.getHourlyRate());
        mentor.setStatus(MentorStatus.PENDING);
        mentor.setCreatedAt(LocalDateTime.now());

        Mentor saved = mentorRepository.save(mentor);

        return mapToResponseDTO(saved);
    }

    @Override
    @Caching(
            put = @CachePut(value = "mentorsById", key = "#result.id"),
            evict = @CacheEvict(value = "allMentors", allEntries = true)
    )
    public MentorResponseDTO approveMentor(Long id, String role, String authorizationHeader) {
        requireAdmin(role);

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Authorization header is required");
        }

        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        if (mentor.getStatus() != MentorStatus.PENDING) {
            throw new BadRequestException("Only pending mentor applications can be approved");
        }

        mentor.setStatus(MentorStatus.APPROVED);
        Mentor updated = mentorRepository.save(mentor);

        ResponseDTO<?> promoteResponse;
        try {
            promoteResponse = authClient.promoteUserToMentor(updated.getUserId(), authorizationHeader);
        } catch (Exception ex) {
            throw new BadRequestException("Mentor approved, but role promotion failed in auth service");
        }

        if (promoteResponse == null || !promoteResponse.isSuccess()) {
            throw new BadRequestException("Mentor approved, but role promotion failed in auth service");
        }

        return mapToResponseDTO(updated);
    }

    @Override
    @Caching(
            put = @CachePut(value = "mentorsById", key = "#result.id"),
            evict = @CacheEvict(value = "allMentors", allEntries = true)
    )
    public MentorResponseDTO rejectMentor(Long id, String role) {
        requireAdmin(role);

        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        if (mentor.getStatus() != MentorStatus.PENDING) {
            throw new BadRequestException("Only pending mentor applications can be rejected");
        }

        mentor.setStatus(MentorStatus.REJECTED);
        Mentor updated = mentorRepository.save(mentor);
        return mapToResponseDTO(updated);
    }

    @Override
    @Cacheable(value = "mentorsById", key = "#id")
    public MentorResponseDTO getMentorById(Long id) {

        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        return mapToResponseDTO(mentor);
    }

    @Override
    public MentorResponseDTO getMentorByUserId(Long userId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with user id: " + userId));
        return mapToResponseDTO(mentor);
    }

    @Override
    @Cacheable(value = "allMentors")
    public List<MentorResponseDTO> getAllMentors() {

        List<Mentor> mentors = mentorRepository.findAll();

        return mentors.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Caching(
            put = @CachePut(value = "mentorsById", key = "#result.id"),
            evict = @CacheEvict(value = "allMentors", allEntries = true)
    )
    public MentorResponseDTO updateMentor(Long id, Long userId, MentorRequestDTO requestDTO) {

        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        if (!mentor.getUserId().equals(userId)) {
            throw new BadRequestException("You are not allowed to update this mentor profile");
        }

        mentor.setBio(requestDTO.getBio());
        mentor.setExperience(requestDTO.getExperience());
        mentor.setHourlyRate(requestDTO.getHourlyRate());

        Mentor updated = mentorRepository.save(mentor);

        return mapToResponseDTO(updated);
    }

    private MentorResponseDTO mapToResponseDTO(Mentor mentor) {
        MentorResponseDTO dto = new MentorResponseDTO();
        dto.setId(mentor.getId());
        dto.setUserId(mentor.getUserId());
        dto.setBio(mentor.getBio());
        dto.setExperience(mentor.getExperience());
        dto.setHourlyRate(mentor.getHourlyRate());
        dto.setStatus(mentor.getStatus());
        return dto;
    }

    private void requireAdmin(String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            throw new UnauthorizedException("Only admin can verify mentor applications");
        }
    }
}
