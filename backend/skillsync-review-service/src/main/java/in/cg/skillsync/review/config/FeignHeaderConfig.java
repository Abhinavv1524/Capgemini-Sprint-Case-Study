package in.cg.skillsync.review.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignHeaderConfig {

    @Bean
    public RequestInterceptor gatewayHeaderForwardingInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String userId = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role");

            if (StringUtils.hasText(userId)) {
                template.header("X-User-Id", userId);
            }
            if (StringUtils.hasText(role)) {
                template.header("X-User-Role", role);
            }
        };
    }
}
