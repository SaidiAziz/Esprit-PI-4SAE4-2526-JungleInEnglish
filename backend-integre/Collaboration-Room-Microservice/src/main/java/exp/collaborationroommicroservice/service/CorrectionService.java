package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.dto.SubmitCorrectionRequest;
import exp.collaborationroommicroservice.entity.PeerCorrection;

import java.util.List;

public interface CorrectionService {

    PeerCorrection submit(Long messageId, Long userId, SubmitCorrectionRequest request);

    List<PeerCorrection> getCorrections(Long messageId);

    PeerCorrection accept(Long correctionId, Long currentUserId);

    PeerCorrection refuse(Long correctionId, Long currentUserId);

    PeerCorrection voteHelpful(Long correctionId);

    void delete(Long correctionId, Long currentUserId);
}
