package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.entity.RoomParticipant;
import exp.collaborationroommicroservice.exception.ResourceNotFoundException;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.ReputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReputationServiceImpl implements ReputationService {

    private final RoomParticipantRepository participantRepository;

    @Override
    public RoomParticipant onMessageSent(Long roomId, Long userId) {
        RoomParticipant participant = getParticipant(roomId, userId);
        if (participant.getMessagesCount() >= 7) {
            participant.setReputationScore(participant.getReputationScore() + 3);
        } else {
            participant.setReputationScore(participant.getReputationScore() + 1);
        }
        return participantRepository.save(participant);
    }

    @Override
    public RoomParticipant onHelpfulVote(Long roomId, Long userId) {
        RoomParticipant participant = getParticipant(roomId, userId);
        participant.setReputationScore(participant.getReputationScore() + 2);
        return participantRepository.save(participant);
    }

    @Override
    public RoomParticipant onCorrectionAccepted(Long roomId, Long userId) {
        RoomParticipant participant = getParticipant(roomId, userId);
        participant.setReputationScore(participant.getReputationScore() + 5);
        return participantRepository.save(participant);
    }

    @Override
    public RoomParticipant onCorrectionRefused(Long roomId, Long userId) {
        RoomParticipant participant = getParticipant(roomId, userId);
        participant.setReputationScore(Math.max(0, participant.getReputationScore() - 1));
        return participantRepository.save(participant);
    }

    @Override
    public RoomParticipant onChallengeWon(Long roomId, Long userId) {
        RoomParticipant participant = getParticipant(roomId, userId);
        participant.setReputationScore(participant.getReputationScore() + 10);
        return participantRepository.save(participant);
    }

    private RoomParticipant getParticipant(Long roomId, Long userId) {
        return participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found in room"));
    }
}
