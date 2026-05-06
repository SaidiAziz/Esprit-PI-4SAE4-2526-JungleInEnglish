package exp.ailearningassistantmicroservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePerformanceAnalysisRequest {

    @NotNull
    private Long courseId;

    @Min(0)
    @Max(100)
    private int grammarScore;

    @Min(0)
    @Max(100)
    private int listeningScore;

    @Min(0)
    @Max(100)
    private int speakingScore;
}
