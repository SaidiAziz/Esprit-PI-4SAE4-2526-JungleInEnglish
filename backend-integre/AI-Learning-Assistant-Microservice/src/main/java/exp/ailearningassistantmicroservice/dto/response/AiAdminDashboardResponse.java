package exp.ailearningassistantmicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiAdminDashboardResponse {

    private final long totalStudents;
    private final long totalAnalyses;
    private final long totalRecommendations;
    private final long totalLearningPaths;
    private final List<AiAdminMetricResponse> levelDistribution;
    private final List<AiAdminMetricResponse> weakestSkillDistribution;
    private final List<PerformanceAnalysisResponse> recentAnalyses;
}
