package exp.ailearningassistantmicroservice.dto.response;

import exp.ailearningassistantmicroservice.entities.StudentLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PerformanceAnalysisResponse {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final int grammarScore;
    private final int listeningScore;
    private final int speakingScore;
    private final double averageScore;
    private final StudentLevel level;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastUpdated;
}
