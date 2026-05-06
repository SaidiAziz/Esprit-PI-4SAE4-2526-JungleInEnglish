package exp.collaborationroommicroservice.dto.response;

import exp.collaborationroommicroservice.entity.CorrectionErrorType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CorrectionResponse {

    private final Long id;
    private final Long messageId;
    private final Long correctorId;
    private final String originalText;
    private final String correctedText;
    private final String explanation;
    private final CorrectionErrorType errorType;
    private final boolean accepted;
    private final int helpfulVotes;
    private final LocalDateTime correctedAt;
}
