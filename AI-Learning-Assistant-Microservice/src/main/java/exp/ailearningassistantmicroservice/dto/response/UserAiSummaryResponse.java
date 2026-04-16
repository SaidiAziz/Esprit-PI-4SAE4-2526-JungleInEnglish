package exp.ailearningassistantmicroservice.dto.response;

import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.entities.StudentLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserAiSummaryResponse {

    private final Long userId;
    private final StudentLevel currentLevel;
    private final double overallAverage;
    private final SkillType weakestSkill;
    private final int totalAnalyses;
    private final List<PerformanceAnalysisResponse> performanceAnalyses;
    private final List<RecommendationResponse> recommendations;
    private final List<LearningPathResponse> learningPaths;
}
