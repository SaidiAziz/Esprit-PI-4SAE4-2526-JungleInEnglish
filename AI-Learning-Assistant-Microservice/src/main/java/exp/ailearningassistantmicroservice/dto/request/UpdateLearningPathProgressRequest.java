package exp.ailearningassistantmicroservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLearningPathProgressRequest {

    @Min(0)
    @Max(100)
    private double progress;
}
