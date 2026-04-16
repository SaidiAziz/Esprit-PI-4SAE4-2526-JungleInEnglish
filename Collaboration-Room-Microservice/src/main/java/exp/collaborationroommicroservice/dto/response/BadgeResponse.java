package exp.collaborationroommicroservice.dto.response;

import exp.collaborationroommicroservice.entity.BadgeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BadgeResponse {

    private final Long id;
    private final Long userId;
    private final BadgeType badgeType;
    private final Long roomId;
    private final LocalDateTime earnedAt;
}
