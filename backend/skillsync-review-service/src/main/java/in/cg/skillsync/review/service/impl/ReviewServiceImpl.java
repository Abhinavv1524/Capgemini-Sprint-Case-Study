package in.cg.skillsync.review.service.impl;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.review.client.SessionClient;
import in.cg.skillsync.review.dto.*;
import in.cg.skillsync.review.entity.Review;
import in.cg.skillsync.review.repository.ReviewRepository;
import in.cg.skillsync.review.service.ReviewService;
import in.cg.skillsync.review.util.SecurityUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

	@Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private SessionClient sessionClient;

    @Override
    @CircuitBreaker(name = "sessionService", fallbackMethod = "addReviewFallback")
    @Caching(evict = {
            @CacheEvict(value = "reviewsByMentor", key = "#request.mentorId"),
            @CacheEvict(value = "mentorRatings", key = "#request.mentorId")
    })
    public ReviewResponseDTO addReview(AddReviewRequestDTO request, HttpServletRequest servletRequest) {
        Long userId = getRequiredUserId(servletRequest);
        String role = SecurityUtil.getCurrentUserRole(servletRequest);

        // 1. Role validation
        if (!"ROLE_LEARNER".equals(role)) {
            throw new UnauthorizedException("Only learners can add reviews");
        }

        // 2. Validate session via Feign
        SessionDTO session;
        try {
        	ResponseDTO<SessionDTO> response = sessionClient.getSessionById(
                    request.getSessionId(),
                    String.valueOf(userId),
                    role
            );

        	session = response.getData();
        } catch (Exception e) {
            throw new BadRequestException("Invalid session ID or Session Service unavailable");
        }

        if (session == null) {
            throw new BadRequestException("Session not found");
        }

        // 3. Ownership validation
        if (!session.getLearnerId().equals(userId)) {
            throw new BadRequestException("You can only review your own session");
        }

        // Ensure mentor in request matches mentor on the session.
        if (!session.getMentorId().equals(request.getMentorId())) {
            throw new BadRequestException("Mentor ID does not match session mentor");
        }

        // 4. (Optional — can add later)
        // if (!"COMPLETED".equals(session.getStatus())) {
        //     throw new BadRequestException("You can only review completed sessions");
        // }

        // 5. Duplicate check
        if (reviewRepository.existsByUserIdAndSessionId(userId, request.getSessionId())) {
            throw new BadRequestException("Review already exists for this session");
        }

        // 6. Create entity
        Review review = new Review(
                session.getMentorId(),
                userId,
                request.getSessionId(),
                request.getRating(),
                request.getComment(),
                LocalDateTime.now()
        );

        Review saved = reviewRepository.save(review);

        return mapToDTO(saved);
    }

    @Override
    @Cacheable(value = "reviewsByMentor", key = "#mentorId")
    public List<ReviewResponseDTO> getReviewsByMentor(Long mentorId) {
        return reviewRepository.findByMentorId(mentorId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "mentorRatings", key = "#mentorId")
    public RatingResponseDTO getRatingByMentor(Long mentorId) {

        Double avg = reviewRepository.findAverageRatingByMentorId(mentorId);
        Long count = (long) reviewRepository.findByMentorId(mentorId).size();

        return new RatingResponseDTO(
                avg != null ? avg : 0.0,
                count
        );
    }

    // Mapper
    private ReviewResponseDTO mapToDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setMentorId(review.getMentorId());
        dto.setUserId(review.getUserId());
        dto.setSessionId(review.getSessionId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    private Long getRequiredUserId(HttpServletRequest request) {
        String userId = SecurityUtil.getCurrentUserId(request);
        try {
            return Long.parseLong(userId);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid user context");
        }
    }

    @SuppressWarnings("unused")
    private ReviewResponseDTO addReviewFallback(AddReviewRequestDTO request,
                                                HttpServletRequest servletRequest,
                                                Throwable throwable) {
        // IMPORTANT:
        // @CircuitBreaker fallback runs for any exception from addReview(), not just session-service outages.
        // Preserve real validation/auth errors so callers can fix inputs; only mask genuine downstream outages.

        if (throwable instanceof UnauthorizedException) {
            throw (UnauthorizedException) throwable;
        }
        if (throwable instanceof BadRequestException) {
            throw (BadRequestException) throwable;
        }

        throw new BadRequestException("Session service is temporarily unavailable. Please try again.");
    }
}
