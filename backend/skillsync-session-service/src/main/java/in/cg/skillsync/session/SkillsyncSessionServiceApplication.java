package in.cg.skillsync.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableCaching
public class SkillsyncSessionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillsyncSessionServiceApplication.class, args);
	}

}
