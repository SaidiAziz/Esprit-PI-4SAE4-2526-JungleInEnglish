package exp.collaborationroommicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BadgeLeaderboardResponse {

    private final Long userId;
    private final Long badgeCount;
}
