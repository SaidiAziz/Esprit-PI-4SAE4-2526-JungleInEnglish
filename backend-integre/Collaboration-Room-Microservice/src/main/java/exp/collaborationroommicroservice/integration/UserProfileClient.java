package exp.collaborationroommicroservice.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "USER-SERVICE", configuration = FeignForwardAuthConfig.class)
public interface UserProfileClient {

    @GetMapping("/studentProfile/me")
    ResponseEntity<Map<String, Object>> getCurrentStudentProfile();
}
