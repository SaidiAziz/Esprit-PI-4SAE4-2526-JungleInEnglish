package exp.collaborationroommicroservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeAnswerResponse {

    private final Long challengeId;
    private final Long userId;
    private final boolean correct;
    private final String feedback;
    private final int pointsAwarded;
}
