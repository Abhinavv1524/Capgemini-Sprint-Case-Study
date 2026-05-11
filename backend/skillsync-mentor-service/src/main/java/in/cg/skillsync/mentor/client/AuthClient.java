package in.cg.skillsync.mentor.client;

import in.cg.skillsync.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PutMapping("/api/auth/users/{userId}/promote-mentor")
    ResponseDTO<?> promoteUserToMentor(
            @PathVariable("userId") Long userId,
            @RequestHeader("Authorization") String authorizationHeader
    );
}
