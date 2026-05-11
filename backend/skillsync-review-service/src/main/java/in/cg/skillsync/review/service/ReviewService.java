package in.cg.skillsync.review.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import in.cg.skillsync.review.dto.AddReviewRequestDTO;
import in.cg.skillsync.review.dto.RatingResponseDTO;
import in.cg.skillsync.review.dto.ReviewResponseDTO;

public interface ReviewService {

    ReviewResponseDTO addReview(AddReviewRequestDTO request, HttpServletRequest servletRequest);
    
    List<ReviewResponseDTO> getReviewsByMentor(Long mentorId);

    RatingResponseDTO getRatingByMentor(Long mentorId);
}
