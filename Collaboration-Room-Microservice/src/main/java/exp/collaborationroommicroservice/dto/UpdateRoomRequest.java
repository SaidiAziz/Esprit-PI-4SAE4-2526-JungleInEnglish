package exp.collaborationroommicroservice.dto;

import exp.collaborationroommicroservice.entity.RoomLevel;
import exp.collaborationroommicroservice.entity.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoomRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String nativeLanguage;

    @NotBlank
    private String targetLanguage;

    @NotNull
    private RoomLevel level;

    @NotNull
    private RoomType type;

    @Min(2)
    private int maxParticipants;

    private boolean isPublic;
    private String topic;
    private Long courseId;
}
