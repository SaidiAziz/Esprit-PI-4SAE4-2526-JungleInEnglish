package exp.collaborationroommicroservice.dto;

import exp.collaborationroommicroservice.entity.ParticipantRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRoomRequest {

    @NotNull
    private Long userId;

    private ParticipantRole role = ParticipantRole.LEARNER;
}
