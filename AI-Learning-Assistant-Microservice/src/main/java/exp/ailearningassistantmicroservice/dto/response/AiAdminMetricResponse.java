package exp.ailearningassistantmicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiAdminMetricResponse {

    private final String label;
    private final long value;
}
