package exp.collaborationroommicroservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMessageFlagsRequest {

    private boolean translationRequest;
    private boolean correctionRequest;
}
