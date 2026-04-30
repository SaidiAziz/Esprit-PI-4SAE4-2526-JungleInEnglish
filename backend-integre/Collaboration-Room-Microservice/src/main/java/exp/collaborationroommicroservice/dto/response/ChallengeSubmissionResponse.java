package exp.collaborationroommicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChallengeSubmissionResponse {

    private final Long id;
    private final Long challengeId;
    private final Long userId;
    private final String answer;
    private final boolean correct;
    private final String feedback;
    private final int pointsAwarded;
    private final LocalDateTime submittedAt;
}
