package in.cg.skillsync.notification.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignHeaderConfig {

    @Bean
    public RequestInterceptor serviceHeaderInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                String userId = attributes.getRequest().getHeader("X-User-Id");
                String role = attributes.getRequest().getHeader("X-User-Role");

                if (StringUtils.hasText(userId)) {
                    template.header("X-User-Id", userId);
                }
                if (StringUtils.hasText(role)) {
                    template.header("X-User-Role", role);
                }
                return;
            }

            template.header("X-User-Id", "0");
            template.header("X-User-Role", "ROLE_ADMIN");
        };
    }
}
