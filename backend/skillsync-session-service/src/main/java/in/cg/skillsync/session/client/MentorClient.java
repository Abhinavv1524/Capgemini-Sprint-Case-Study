package in.cg.skillsync.session.client;

import in.cg.skillsync.common.dto.ResponseDTO;
import in.cg.skillsync.session.dto.MentorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    @GetMapping("/api/mentors/{id}")
    ResponseDTO<MentorDTO> getMentorById(@PathVariable("id") Long id);

    @GetMapping("/api/mentors/user/{userId}")
    ResponseDTO<MentorDTO> getMentorByUserId(@PathVariable("userId") Long userId);
}
