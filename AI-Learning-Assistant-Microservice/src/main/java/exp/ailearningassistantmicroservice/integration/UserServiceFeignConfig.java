package exp.ailearningassistantmicroservice.integration;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserServiceFeignConfig {

    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return template -> {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                HttpServletRequest request = servletAttributes.getRequest();
                String authorization = request.getHeader("Authorization");
                if (authorization != null && !authorization.isBlank()) {
                    template.header("Authorization", authorization);
                }
            }
        };
    }
}
