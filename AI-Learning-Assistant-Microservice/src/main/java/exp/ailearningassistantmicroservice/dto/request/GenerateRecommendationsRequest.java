package exp.ailearningassistantmicroservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateRecommendationsRequest {

    @NotNull
    private Long userId;
}
