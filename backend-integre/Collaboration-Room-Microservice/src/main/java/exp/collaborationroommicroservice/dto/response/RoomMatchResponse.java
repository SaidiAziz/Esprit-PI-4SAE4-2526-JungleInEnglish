package exp.collaborationroommicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomMatchResponse {

    private final RoomResponse room;
    private final String recommendationReason;
    private final int priorityScore;
}
