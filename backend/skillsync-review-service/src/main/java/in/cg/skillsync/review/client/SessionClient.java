package in.cg.skillsync.review.client;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.review.dto.SessionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "session-service")
public interface SessionClient {

	@GetMapping("/api/sessions/{id}")
	ResponseDTO<SessionDTO> getSessionById(
            @PathVariable("id") Long sessionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole
    );
}
