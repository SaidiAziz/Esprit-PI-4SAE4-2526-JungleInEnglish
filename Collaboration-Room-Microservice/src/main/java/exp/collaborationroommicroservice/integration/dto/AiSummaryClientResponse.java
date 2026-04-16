package exp.collaborationroommicroservice.integration.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiSummaryClientResponse {

    private Long userId;
    private String currentLevel;
    private double overallAverage;
    private String weakestSkill;
    private int totalAnalyses;
}
