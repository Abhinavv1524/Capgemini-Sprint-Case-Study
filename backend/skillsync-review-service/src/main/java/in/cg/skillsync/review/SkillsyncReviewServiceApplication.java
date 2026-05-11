package in.cg.skillsync.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableCaching
public class SkillsyncReviewServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncReviewServiceApplication.class, args);
	}

}
