package in.cg.skillsync.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.cg.skillsync.session.entity.Session;
import java.util.List;


public interface SessionRepository extends JpaRepository<Session, Long> {
	List<Session> findByMentorId(Long mentorId);
	List<Session> findByLearnerId(Long learnerId);
}
