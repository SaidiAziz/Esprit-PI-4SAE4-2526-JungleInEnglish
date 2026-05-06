package exp.collaborationroommicroservice.dto;

import exp.collaborationroommicroservice.entity.ChallengeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateChallengeStatusRequest {

    @NotNull
    private ChallengeStatus status;
}
