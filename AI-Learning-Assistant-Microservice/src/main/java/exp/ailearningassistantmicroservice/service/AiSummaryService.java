package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.dto.response.UserAiSummaryResponse;

public interface AiSummaryService {

    UserAiSummaryResponse getUserSummary(Long userId);
}
