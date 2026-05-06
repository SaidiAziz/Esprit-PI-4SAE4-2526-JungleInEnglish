package exp.collaborationroommicroservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    @NotBlank
    private String content;

    @NotBlank
    private String language;

    private boolean translationRequest;
    private boolean correctionRequest;
    private String mediaUrl;
}
