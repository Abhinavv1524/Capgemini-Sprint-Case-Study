package in.cg.skillsync.skill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "in.cg.skillsync")
@EnableDiscoveryClient
@EnableCaching
public class SkillsyncSkillServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncSkillServiceApplication.class, args);
	}

}
