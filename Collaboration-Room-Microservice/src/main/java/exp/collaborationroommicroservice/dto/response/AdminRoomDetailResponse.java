package exp.collaborationroommicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminRoomDetailResponse {

    private final AdminRoomOverviewResponse overview;
    private final List<ParticipantResponse> participants;
    private final List<MessageResponse> recentMessages;
    private final List<CorrectionResponse> recentCorrections;
    private final List<ChallengeResponse> recentChallenges;
}
