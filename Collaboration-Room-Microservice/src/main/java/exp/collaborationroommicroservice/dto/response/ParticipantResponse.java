package exp.collaborationroommicroservice.dto.response;

import exp.collaborationroommicroservice.entity.ParticipantRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ParticipantResponse {

    private final Long id;
    private final Long roomId;
    private final Long userId;
    private final ParticipantRole role;
    private final LocalDateTime joinedAt;
    private final LocalDateTime lastActiveAt;
    private final int messagesCount;
    private final int correctionsGiven;
    private final int correctionsReceived;
    private final double reputationScore;
}
