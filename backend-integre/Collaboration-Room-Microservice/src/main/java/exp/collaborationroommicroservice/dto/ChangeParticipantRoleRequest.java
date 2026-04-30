package exp.collaborationroommicroservice.dto;

import exp.collaborationroommicroservice.entity.ParticipantRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeParticipantRoleRequest {

    @NotNull
    private ParticipantRole role;
}
