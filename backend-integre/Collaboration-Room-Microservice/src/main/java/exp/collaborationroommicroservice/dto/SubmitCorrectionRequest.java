package exp.collaborationroommicroservice.dto;

import exp.collaborationroommicroservice.entity.CorrectionErrorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitCorrectionRequest {

    @NotBlank
    private String correctedText;

    @NotBlank
    private String explanation;

    @NotNull
    private CorrectionErrorType errorType;
}
