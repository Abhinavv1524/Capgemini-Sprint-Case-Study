package in.cg.skillsync.review.dto;

public class RatingResponseDTO {

    private Double averageRating;
    private Long totalReviews;

    public RatingResponseDTO(Double averageRating, Long totalReviews) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

	public Double getAverageRating() {
		return averageRating;
	}

	public Long getTotalReviews() {
		return totalReviews;
	}
    
    

}