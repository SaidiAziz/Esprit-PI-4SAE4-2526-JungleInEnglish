package exp.ailearningassistantmicroservice.dto.response;

import exp.ailearningassistantmicroservice.entities.RecommendationType;
import exp.ailearningassistantmicroservice.entities.SkillType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecommendationResponse {

    private final Long id;
    private final Long userId;
    private final RecommendationType type;
    private final Long contentId;
    private final String contentTitle;
    private final String contentDescription;
    private final String contentLevel;
    private final Double contentPrice;
    private final String reason;
    private final SkillType focusSkill;
    private final LocalDateTime createdAt;
}
