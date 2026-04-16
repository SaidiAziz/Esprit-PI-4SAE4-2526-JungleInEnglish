package exp.collaborationroommicroservice.dto.response;

import exp.collaborationroommicroservice.entity.RoomLevel;
import exp.collaborationroommicroservice.entity.RoomStatus;
import exp.collaborationroommicroservice.entity.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomResponse {

    private final Long id;
    private final String title;
    private final String nativeLanguage;
    private final String targetLanguage;
    private final RoomLevel level;
    private final RoomType type;
    private final int maxParticipants;
    private final boolean isPublic;
    private final String topic;
    private final RoomStatus status;
    private final Long courseId;
    private final Long createdBy;
    private final LocalDateTime createdAt;
}
