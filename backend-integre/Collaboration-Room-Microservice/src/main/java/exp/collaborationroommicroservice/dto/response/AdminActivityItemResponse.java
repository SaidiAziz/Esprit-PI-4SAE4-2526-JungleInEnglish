package exp.collaborationroommicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminActivityItemResponse {

    private final String type;
    private final Long roomId;
    private final Long entityId;
    private final Long actorUserId;
    private final String description;
    private final LocalDateTime createdAt;
}
