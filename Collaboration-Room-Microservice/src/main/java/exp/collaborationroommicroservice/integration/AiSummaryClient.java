package exp.collaborationroommicroservice.integration;

import exp.collaborationroommicroservice.integration.dto.AiSummaryClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "AI-LEARNING-ASSISTANT-SERVICE",
        contextId = "aiSummaryClient",
        configuration = FeignForwardAuthConfig.class
)
public interface AiSummaryClient {

    @GetMapping("/api/ai/summary/me")
    AiSummaryClientResponse getCurrentSummary();
}
