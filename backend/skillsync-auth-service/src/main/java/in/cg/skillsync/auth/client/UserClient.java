package in.cg.skillsync.auth.client;

import in.cg.skillsync.auth.dto.UserProfileSyncRequest;
import in.cg.skillsync.common.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/users")
    ResponseDTO<?> createUserProfile(
            @RequestHeader("X-User-Id") Long authUserId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody UserProfileSyncRequest request
    );
}
