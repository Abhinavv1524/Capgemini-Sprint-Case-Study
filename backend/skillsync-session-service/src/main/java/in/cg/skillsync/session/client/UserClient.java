package in.cg.skillsync.session.client;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.session.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/auth/{authUserId}")
    ResponseDTO<UserDTO> getUserByAuthUserId(@PathVariable("authUserId") Long authUserId);
}
