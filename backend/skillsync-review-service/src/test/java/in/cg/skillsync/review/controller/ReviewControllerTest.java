package in.cg.skillsync.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.skillsync.common.exception.UnauthorizedException;
import in.cg.skillsync.review.config.SecurityConfig;
import in.cg.skillsync.review.dto.AddReviewRequestDTO;
import in.cg.skillsync.review.dto.ReviewResponseDTO;
import in.cg.skillsync.review.exception.GlobalExceptionHandler;
import in.cg.skillsync.review.service.ReviewService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void testAddReview_Success() throws Exception {
        AddReviewRequestDTO request = validRequest();
        ReviewResponseDTO response = new ReviewResponseDTO();
        response.setId(1L);
        response.setMentorId(20L);
        response.setUserId(10L);
        response.setSessionId(100L);
        response.setRating(5);
        response.setComment("Great");
        response.setCreatedAt(LocalDateTime.now());

        when(reviewService.addReview(any(AddReviewRequestDTO.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testAddReview_InvalidRequest() throws Exception {
        AddReviewRequestDTO request = validRequest();
        request.setRating(0);

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetReviews_Unauthorized_NoHeaders() throws Exception {
        mockMvc.perform(get("/api/reviews/mentor/20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddReview_Forbidden() throws Exception {
        when(reviewService.addReview(any(AddReviewRequestDTO.class), any()))
                .thenThrow(new UnauthorizedException("Only learners can add reviews"));

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .with(user("mentor").roles("MENTOR"))
                        .header("X-User-Id", "20")
                        .header("X-User-Role", "ROLE_MENTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testGetReviewsByMentor_Success() throws Exception {
        ReviewResponseDTO review = new ReviewResponseDTO();
        review.setId(1L);
        review.setMentorId(20L);
        review.setRating(5);
        review.setComment("Great");

        when(reviewService.getReviewsByMentor(20L))
                .thenReturn(java.util.List.of(review));

        mockMvc.perform(get("/api/reviews/mentor/20")
                        .with(user("learner").roles("LEARNER"))
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "ROLE_LEARNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].comment").value("Great"));
    }

    private AddReviewRequestDTO validRequest() {
        AddReviewRequestDTO request = new AddReviewRequestDTO();
        request.setMentorId(20L);
        request.setSessionId(100L);
        request.setRating(5);
        request.setComment("Great");
        return request;
    }
}
