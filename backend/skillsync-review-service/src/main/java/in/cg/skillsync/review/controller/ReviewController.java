package in.cg.skillsync.review.controller;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.review.dto.*;
import in.cg.skillsync.review.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Add Review
    @PostMapping
    public ResponseEntity<ResponseDTO<ReviewResponseDTO>> addReview(
            @Valid @RequestBody AddReviewRequestDTO request,
            HttpServletRequest servletRequest
    ) {

        ReviewResponseDTO response = reviewService.addReview(request, servletRequest);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Review added successfully", response)
        );
    }

    // Get Reviews by Mentor
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ResponseDTO<List<ReviewResponseDTO>>> getReviewsByMentor(
            @PathVariable Long mentorId
    ) {

        List<ReviewResponseDTO> reviews = reviewService.getReviewsByMentor(mentorId);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Reviews fetched successfully", reviews)
        );
    }

    // Get Rating
    @GetMapping("/mentor/{mentorId}/rating")
    public ResponseEntity<ResponseDTO<RatingResponseDTO>> getRating(
            @PathVariable Long mentorId
    ) {

        RatingResponseDTO rating = reviewService.getRatingByMentor(mentorId);

        return ResponseEntity.ok(
                new ResponseDTO<>(true, "Rating fetched successfully", rating)
        );
    }
}
