package exp.collaborationroommicroservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponse {

    private final Long id;
    private final Long roomId;
    private final Long senderId;
    private final String content;
    private final String language;
    private final boolean translationRequest;
    private final String autoTranslation;
    private final boolean correctionRequest;
    private final String mediaUrl;
    private final LocalDateTime sentAt;
}
