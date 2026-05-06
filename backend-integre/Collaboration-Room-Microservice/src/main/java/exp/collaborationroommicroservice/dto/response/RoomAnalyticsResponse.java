package exp.collaborationroommicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomAnalyticsResponse {

    private final Long roomId;
    private final long totalMessages;
    private final double avgResponseTimeMinutes;
    private final Long topContributorUserId;
    private final String mostActiveLanguage;
    private final double correctionsAcceptanceRate;
    private final double challengeCompletionRate;
    private final Integer peakHour;
}
