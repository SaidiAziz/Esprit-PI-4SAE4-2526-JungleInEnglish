package exp.collaborationroommicroservice.dto;

import exp.collaborationroommicroservice.entity.ChallengeDifficulty;
import exp.collaborationroommicroservice.entity.ChallengeType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateChallengeRequest {

    @NotNull
    private ChallengeType type;

    @NotBlank
    private String prompt;

    private String correctAnswer;

    @NotNull
    @Future
    private LocalDateTime deadline;

    @NotNull
    private ChallengeDifficulty difficulty;

    private int pointsReward;
}
