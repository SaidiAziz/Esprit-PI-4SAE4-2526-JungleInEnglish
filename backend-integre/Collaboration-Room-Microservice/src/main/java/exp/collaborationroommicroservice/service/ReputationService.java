package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.entity.RoomParticipant;

public interface ReputationService {

    RoomParticipant onMessageSent(Long roomId, Long userId);

    RoomParticipant onHelpfulVote(Long roomId, Long userId);

    RoomParticipant onCorrectionAccepted(Long roomId, Long userId);

    RoomParticipant onCorrectionRefused(Long roomId, Long userId);

    RoomParticipant onChallengeWon(Long roomId, Long userId);
}
