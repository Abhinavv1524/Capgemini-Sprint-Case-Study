package in.cg.skillsync.mentor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
public class SkillsyncMentorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncMentorServiceApplication.class, args);
	}

}
