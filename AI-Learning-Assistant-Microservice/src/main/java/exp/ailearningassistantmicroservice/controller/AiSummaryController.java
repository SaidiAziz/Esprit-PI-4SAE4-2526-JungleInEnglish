package exp.ailearningassistantmicroservice.controller;

import exp.ailearningassistantmicroservice.dto.response.UserAiSummaryResponse;
import exp.ailearningassistantmicroservice.security.SecurityUtils;
import exp.ailearningassistantmicroservice.service.AiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/summary")
@RequiredArgsConstructor
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;

    @GetMapping("/user/{userId}")
    public UserAiSummaryResponse getByUser(@PathVariable Long userId) {
        return aiSummaryService.getUserSummary(userId);
    }

    @GetMapping("/me")
    public UserAiSummaryResponse getMine() {
        return aiSummaryService.getUserSummary(SecurityUtils.getCurrentUserId());
    }
}
