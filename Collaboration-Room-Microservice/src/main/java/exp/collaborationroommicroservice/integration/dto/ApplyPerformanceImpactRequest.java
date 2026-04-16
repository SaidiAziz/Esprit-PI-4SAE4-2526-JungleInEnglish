package exp.collaborationroommicroservice.integration.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplyPerformanceImpactRequest {

    private final Long userId;
    private final Long courseId;
    private final int grammarDelta;
    private final int listeningDelta;
    private final int speakingDelta;
}
