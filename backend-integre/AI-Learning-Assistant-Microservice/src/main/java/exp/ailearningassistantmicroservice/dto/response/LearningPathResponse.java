package exp.ailearningassistantmicroservice.dto.response;

import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.entities.StudentLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LearningPathResponse {

    private final Long id;
    private final Long userId;
    private final Long courseId;
    private final String lessonOrder;
    private final double progress;
    private final SkillType focusSkill;
    private final StudentLevel targetLevel;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
