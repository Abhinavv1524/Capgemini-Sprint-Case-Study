package in.cg.skillsync.group.repository;

import in.cg.skillsync.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
	
}