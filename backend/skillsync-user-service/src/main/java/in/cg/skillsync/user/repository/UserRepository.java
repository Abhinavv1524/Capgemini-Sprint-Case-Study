package in.cg.skillsync.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.cg.skillsync.user.entity.User;


public interface UserRepository extends JpaRepository<User, Long>{
	Optional<User> findByAuthUserId(Long authUserId);
}
