package exp.collaborationroommicroservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChallengeAnswerRequest {

    @NotBlank
    private String answer;
}
