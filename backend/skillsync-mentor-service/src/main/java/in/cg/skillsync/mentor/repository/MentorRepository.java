package in.cg.skillsync.mentor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.cg.skillsync.mentor.entity.Mentor;


public interface MentorRepository extends JpaRepository<Mentor, Long>{
	Optional<Mentor> findByUserId(Long userId);
}
