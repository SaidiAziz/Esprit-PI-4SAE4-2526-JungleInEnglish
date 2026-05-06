package exp.collaborationroommicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminDashboardResponse {

    private final long totalRooms;
    private final long activeRooms;
    private final long totalParticipants;
    private final long totalMessages;
    private final long totalCorrections;
    private final long totalChallenges;
    private final long totalChallengeSubmissions;
    private final List<AdminRoomOverviewResponse> topRooms;
}
