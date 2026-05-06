package exp.collaborationroommicroservice.integration;

import exp.collaborationroommicroservice.integration.dto.ApplyPerformanceImpactRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "AI-LEARNING-ASSISTANT-SERVICE",
        contextId = "aiImpactClient",
        configuration = FeignForwardAuthConfig.class
)
public interface AiServiceClient {

    @PostMapping("/api/ai/performance-analyses/impact")
    ResponseEntity<Map<String, Object>> applyImpact(@RequestBody ApplyPerformanceImpactRequest request);
}
