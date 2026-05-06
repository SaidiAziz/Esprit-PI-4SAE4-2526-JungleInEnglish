package exp.ailearningassistantmicroservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateLearningPathRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long courseId;
}
