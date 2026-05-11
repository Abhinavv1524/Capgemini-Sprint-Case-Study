package in.cg.skillsync.review.service;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.common.exception.BadRequestException;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.review.client.SessionClient;
import in.cg.skillsync.review.dto.AddReviewRequestDTO;
import in.cg.skillsync.review.dto.RatingResponseDTO;
import in.cg.skillsync.review.dto.ReviewResponseDTO;
import in.cg.skillsync.review.dto.SessionDTO;
import in.cg.skillsync.review.entity.Review;
import in.cg.skillsync.review.repository.ReviewRepository;
import in.cg.skillsync.review.service.impl.ReviewServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SessionClient sessionClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void testAddReview_Success() {
        AddReviewRequestDTO request = buildRequest();
        HttpServletRequest servletRequest = requestWith("10", "ROLE_LEARNER");

        SessionDTO session = new SessionDTO();
        session.setId(100L);
        session.setLearnerId(10L);
        session.setMentorId(20L);
        session.setStatus("COMPLETED");

        Review saved = new Review(20L, 10L, 100L, 5, "Great", LocalDateTime.now());
        saved.setId(1L);

        when(sessionClient.getSessionById(100L, "10", "ROLE_LEARNER"))
                .thenReturn(new ResponseDTO<>(true, "ok", session));
        when(reviewRepository.existsByUserIdAndSessionId(10L, 100L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        ReviewResponseDTO response = reviewService.addReview(request, servletRequest);

        assertEquals(1L, response.getId());
        assertEquals(5, response.getRating());
        assertEquals(20L, response.getMentorId());
    }

    @Test
    void testAddReview_UnauthorizedRole() {
        HttpServletRequest servletRequest = requestWith("10", "ROLE_MENTOR");
        assertThrows(UnauthorizedException.class, () -> reviewService.addReview(buildRequest(), servletRequest));
    }

    @Test
    void testAddReview_DuplicateReview() {
        AddReviewRequestDTO request = buildRequest();
        HttpServletRequest servletRequest = requestWith("10", "ROLE_LEARNER");

        SessionDTO session = new SessionDTO();
        session.setLearnerId(10L);
        session.setMentorId(20L);
        session.setId(100L);

        when(sessionClient.getSessionById(100L, "10", "ROLE_LEARNER"))
                .thenReturn(new ResponseDTO<>(true, "ok", session));
        when(reviewRepository.existsByUserIdAndSessionId(10L, 100L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> reviewService.addReview(request, servletRequest));
    }

    @Test
    void testGetRatingByMentor_Success() {
        when(reviewRepository.findAverageRatingByMentorId(20L)).thenReturn(4.5);
        when(reviewRepository.findByMentorId(20L)).thenReturn(List.of(
                new Review(20L, 10L, 100L, 5, "Great", LocalDateTime.now()),
                new Review(20L, 11L, 101L, 4, "Good", LocalDateTime.now())
        ));

        RatingResponseDTO response = reviewService.getRatingByMentor(20L);

        assertEquals(4.5, response.getAverageRating());
        assertEquals(2L, response.getTotalReviews());
    }

    private AddReviewRequestDTO buildRequest() {
        AddReviewRequestDTO request = new AddReviewRequestDTO();
        request.setMentorId(20L);
        request.setSessionId(100L);
        request.setRating(5);
        request.setComment("Great");
        return request;
    }

    private HttpServletRequest requestWith(String userId, String role) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-Id")).thenReturn(userId);
        when(request.getHeader("X-User-Role")).thenReturn(role);
        return request;
    }
}
