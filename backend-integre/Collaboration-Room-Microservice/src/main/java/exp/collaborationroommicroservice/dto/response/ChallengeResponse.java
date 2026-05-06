package exp.collaborationroommicroservice.dto.response;

import exp.collaborationroommicroservice.entity.ChallengeDifficulty;
import exp.collaborationroommicroservice.entity.ChallengeStatus;
import exp.collaborationroommicroservice.entity.ChallengeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChallengeResponse {

    private final Long id;
    private final Long roomId;
    private final Long creatorId;
    private final ChallengeType type;
    private final String prompt;
    private final String correctAnswer;
    private final LocalDateTime deadline;
    private final ChallengeDifficulty difficulty;
    private final int pointsReward;
    private final ChallengeStatus status;
}
