package exp.collaborationroommicroservice.dto.response;

import exp.collaborationroommicroservice.entity.RoomStatus;
import exp.collaborationroommicroservice.entity.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminRoomOverviewResponse {

    private final Long id;
    private final String title;
    private final String topic;
    private final RoomType type;
    private final RoomStatus status;
    private final Long createdBy;
    private final Long courseId;
    private final LocalDateTime createdAt;
    private final LocalDateTime latestActivityAt;
    private final long participantCount;
    private final long messageCount;
    private final long correctionCount;
    private final long challengeCount;
}
