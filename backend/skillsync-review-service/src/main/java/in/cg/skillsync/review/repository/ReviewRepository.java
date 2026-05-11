package in.cg.skillsync.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.cg.skillsync.review.entity.Review;
import java.util.List;
import java.util.Optional;


public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByMentorId(Long mentorId);
	
    Optional<Review> findByUserIdAndSessionId(Long userId, Long sessionId);

    boolean existsByUserIdAndSessionId(Long userId, Long sessionId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mentorId = :mentorId")
    Double findAverageRatingByMentorId(@Param("mentorId") Long mentorId);
}
