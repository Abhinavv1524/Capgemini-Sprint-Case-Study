package in.cg.skillsync.notification.client;

import in.cg.skillsync.notification.dto.ResponseDTO;
import in.cg.skillsync.notification.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = in.cg.skillsync.notification.config.FeignHeaderConfig.class)
public interface UserClient {

    @GetMapping("/api/users/auth/{authUserId}")
    ResponseDTO<UserDTO> getUserByAuthUserId(@PathVariable("authUserId") Long authUserId);
}
