package exp.ailearningassistantmicroservice.dto.response;

import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.entities.StudentLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiAdminStudentOverviewResponse {

    private final Long userId;
    private final StudentLevel currentLevel;
    private final double overallAverage;
    private final SkillType weakestSkill;
    private final int analysisCount;
    private final int recommendationCount;
    private final int learningPathCount;
    private final LocalDateTime lastAnalysisAt;
}
