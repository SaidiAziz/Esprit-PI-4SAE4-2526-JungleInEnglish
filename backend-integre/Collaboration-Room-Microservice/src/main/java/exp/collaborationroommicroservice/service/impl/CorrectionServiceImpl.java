package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.SubmitCorrectionRequest;
import exp.collaborationroommicroservice.entity.BadgeType;
import exp.collaborationroommicroservice.entity.LanguageMessage;
import exp.collaborationroommicroservice.entity.PeerCorrection;
import exp.collaborationroommicroservice.entity.RoomParticipant;
import exp.collaborationroommicroservice.exception.BadRequestException;
import exp.collaborationroommicroservice.exception.ResourceNotFoundException;
import exp.collaborationroommicroservice.repository.LanguageMessageRepository;
import exp.collaborationroommicroservice.repository.PeerCorrectionRepository;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.ActivityTrackingService;
import exp.collaborationroommicroservice.service.BadgeService;
import exp.collaborationroommicroservice.service.CorrectionService;
import exp.collaborationroommicroservice.service.ExchangeImpactService;
import exp.collaborationroommicroservice.service.RoomLiveUpdateService;
import exp.collaborationroommicroservice.service.ReputationService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CorrectionServiceImpl implements CorrectionService {

    private final PeerCorrectionRepository correctionRepository;
    private final LanguageMessageRepository messageRepository;
    private final RoomParticipantRepository participantRepository;
    private final BadgeService badgeService;
    private final RoomService roomService;
    private final ReputationService reputationService;
    private final ActivityTrackingService activityTrackingService;
    private final RoomLiveUpdateService roomLiveUpdateService;
    private final ExchangeImpactService exchangeImpactService;

    @Override
    public PeerCorrection submit(Long messageId, Long userId, SubmitCorrectionRequest request) {
        LanguageMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id " + messageId));
        participantRepository.findByRoomIdAndUserId(message.getRoomId(), userId)
                .orElseThrow(() -> new BadRequestException("Join the room before correcting messages"));

        RoomParticipant corrector = participantRepository.findByRoomIdAndUserId(message.getRoomId(), userId).orElseThrow();
        corrector.setCorrectionsGiven(corrector.getCorrectionsGiven() + 1);
        corrector.setLastActiveAt(LocalDateTime.now());
        participantRepository.save(corrector);
        activityTrackingService.track(userId);
        badgeService.evaluateMilestones(userId, message.getRoomId());

        PeerCorrection saved = correctionRepository.save(PeerCorrection.builder()
                .messageId(messageId)
                .correctorId(userId)
                .originalText(message.getContent())
                .correctedText(request.getCorrectedText())
                .explanation(request.getExplanation())
                .errorType(request.getErrorType())
                .accepted(false)
                .build());
        roomLiveUpdateService.publish(message.getRoomId(), "room-update", "correction-submitted");
        return saved;
    }

    @Override
    public List<PeerCorrection> getCorrections(Long messageId) {
        return correctionRepository.findByMessageIdOrderByCorrectedAtDesc(messageId);
    }

    @Override
    public PeerCorrection accept(Long correctionId, Long currentUserId) {
        PeerCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Correction not found with id " + correctionId));
        LanguageMessage message = messageRepository.findById(correction.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found for correction"));
        participantRepository.findByRoomIdAndUserId(message.getRoomId(), currentUserId)
                .orElseThrow(() -> new BadRequestException("Join the room before accepting corrections"));
        if (!message.getSenderId().equals(currentUserId)) {
            throw new BadRequestException("Only the student who received the correction can accept it");
        }
        if (correction.isAccepted()) {
            return correction;
        }

        correction.setAccepted(true);
        PeerCorrection saved = correctionRepository.save(correction);

        RoomParticipant learner = participantRepository.findByRoomIdAndUserId(message.getRoomId(), message.getSenderId())
                .orElseThrow(() -> new BadRequestException("Message author must be a room participant"));
        learner.setCorrectionsReceived(learner.getCorrectionsReceived() + 1);
        participantRepository.save(learner);

        RoomParticipant corrector = participantRepository.findByRoomIdAndUserId(message.getRoomId(), correction.getCorrectorId()).orElseThrow();
        participantRepository.save(corrector);
        reputationService.onCorrectionAccepted(message.getRoomId(), correction.getCorrectorId());
        activityTrackingService.track(currentUserId);

        roomService.getById(message.getRoomId());
        exchangeImpactService.onCorrectionApproved(message.getRoomId(), message.getSenderId());

        if (correctionRepository.countByCorrectorIdAndAcceptedTrue(correction.getCorrectorId()) >= 10) {
            badgeService.awardIfMissing(correction.getCorrectorId(), BadgeType.HELPFUL_CORRECTOR, message.getRoomId());
        }
        roomLiveUpdateService.publish(message.getRoomId(), "room-update", "correction-accepted");
        return saved;
    }

    @Override
    public PeerCorrection refuse(Long correctionId, Long currentUserId) {
        PeerCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Correction not found with id " + correctionId));
        LanguageMessage message = messageRepository.findById(correction.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found for correction"));
        RoomParticipant participant = participantRepository.findByRoomIdAndUserId(message.getRoomId(), currentUserId)
                .orElseThrow(() -> new BadRequestException("Join the room before refusing corrections"));
        if (!message.getSenderId().equals(currentUserId)) {
            throw new BadRequestException("Only the student who received the correction can refuse it");
        }
        if (correction.isAccepted()) {
            throw new BadRequestException("Cannot refuse a correction that has already been accepted");
        }

        reputationService.onCorrectionRefused(message.getRoomId(), correction.getCorrectorId());
        correctionRepository.delete(correction);
        roomLiveUpdateService.publish(message.getRoomId(), "room-update", "correction-refused");
        return correction;
    }

    @Override
    public PeerCorrection voteHelpful(Long correctionId) {
        PeerCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Correction not found with id " + correctionId));
        correction.setHelpfulVotes(correction.getHelpfulVotes() + 1);
        PeerCorrection saved = correctionRepository.save(correction);
        LanguageMessage message = messageRepository.findById(correction.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found for correction"));
        reputationService.onHelpfulVote(message.getRoomId(), correction.getCorrectorId());
        roomLiveUpdateService.publish(message.getRoomId(), "room-update", "correction-voted");
        return saved;
    }

    @Override
    public void delete(Long correctionId, Long currentUserId) {
        PeerCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Correction not found with id " + correctionId));
        if (!correction.getCorrectorId().equals(currentUserId)) {
            throw new BadRequestException("You can only delete your own correction");
        }
        LanguageMessage message = messageRepository.findById(correction.getMessageId())
                .orElseThrow(() -> new ResourceNotFoundException("Message not found for correction"));
        correctionRepository.delete(correction);
        roomLiveUpdateService.publish(message.getRoomId(), "room-update", "correction-deleted");
    }
}
