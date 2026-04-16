package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.dto.response.AiAdminDashboardResponse;
import exp.ailearningassistantmicroservice.dto.response.AiAdminStudentOverviewResponse;
import exp.ailearningassistantmicroservice.dto.response.LearningPathResponse;
import exp.ailearningassistantmicroservice.dto.response.RecommendationResponse;
import exp.ailearningassistantmicroservice.dto.response.UserAiSummaryResponse;

import java.util.List;

public interface AiAdminService {

    AiAdminDashboardResponse getDashboard();

    List<AiAdminStudentOverviewResponse> getStudents();

    UserAiSummaryResponse getStudentDetail(Long userId);

    List<RecommendationResponse> getRecommendations();

    List<RecommendationResponse> regenerateRecommendations(Long userId);

    List<LearningPathResponse> getLearningPaths();

    LearningPathResponse regenerateLearningPath(Long userId, Long courseId);
}
