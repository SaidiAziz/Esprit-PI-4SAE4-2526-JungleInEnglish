package exp.ailearningassistantmicroservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyPerformanceImpactRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long courseId;

    private int grammarDelta;
    private int listeningDelta;
    private int speakingDelta;
}
