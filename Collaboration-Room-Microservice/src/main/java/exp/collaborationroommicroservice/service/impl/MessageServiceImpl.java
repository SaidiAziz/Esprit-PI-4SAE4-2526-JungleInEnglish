package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.SendMessageRequest;
import exp.collaborationroommicroservice.dto.UpdateMessageFlagsRequest;
import exp.collaborationroommicroservice.entity.BadgeType;
import exp.collaborationroommicroservice.entity.LanguageMessage;
import exp.collaborationroommicroservice.entity.ParticipantRole;
import exp.collaborationroommicroservice.entity.RoomParticipant;
import exp.collaborationroommicroservice.exception.BadRequestException;
import exp.collaborationroommicroservice.exception.ResourceNotFoundException;
import exp.collaborationroommicroservice.repository.LanguageMessageRepository;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.ActivityTrackingService;
import exp.collaborationroommicroservice.service.BadgeService;
import exp.collaborationroommicroservice.service.ExchangeImpactService;
import exp.collaborationroommicroservice.service.MessageService;
import exp.collaborationroommicroservice.service.ReputationService;
import exp.collaborationroommicroservice.service.RoomLiveUpdateService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final LanguageMessageRepository messageRepository;
    private final RoomParticipantRepository participantRepository;
    private final RoomService roomService;
    private final BadgeService badgeService;
    private final ExchangeImpactService exchangeImpactService;
    private final ReputationService reputationService;
    private final ActivityTrackingService activityTrackingService;
    private final RoomLiveUpdateService roomLiveUpdateService;

    @Override
    public LanguageMessage send(Long roomId, Long userId, SendMessageRequest request) {
        roomService.getById(roomId);
        RoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BadRequestException("Join the room before sending messages"));

        LanguageMessage message = messageRepository.save(LanguageMessage.builder()
                .roomId(roomId)
                .senderId(userId)
                .content(request.getContent())
                .language(request.getLanguage())
                .translationRequest(request.isTranslationRequest())
                .autoTranslation(request.isTranslationRequest() ? "Auto-translation requested" : null)
                .hasCorrectionRequest(request.isCorrectionRequest())
                .mediaUrl(request.getMediaUrl())
                .build());

        participant.setMessagesCount(participant.getMessagesCount() + 1);
        participant.setLastActiveAt(LocalDateTime.now());
        participantRepository.save(participant);
        reputationService.onMessageSent(roomId, userId);
        activityTrackingService.track(userId);

        if (messageRepository.countBySenderId(userId) == 1) {
            badgeService.awardIfMissing(userId, BadgeType.FIRST_CHAT, roomId);
        }
        badgeService.evaluateMilestones(userId, roomId);
        roomLiveUpdateService.publish(roomId, "room-update", "message-sent");
        return message;
    }

    @Override
    public List<LanguageMessage> getRoomMessages(Long roomId) {
        roomService.getById(roomId);
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    @Override
    public LanguageMessage getById(Long roomId, Long messageId) {
        LanguageMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id " + messageId));
        if (!message.getRoomId().equals(roomId)) {
            throw new BadRequestException("Message does not belong to this room");
        }
        return message;
    }

    @Override
    public LanguageMessage updateFlags(Long messageId, Long userId, UpdateMessageFlagsRequest request) {
        LanguageMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id " + messageId));
        if (!message.getSenderId().equals(userId)) {
            throw new BadRequestException("You can only update your own message");
        }
        message.setTranslationRequest(request.isTranslationRequest());
        message.setAutoTranslation(request.isTranslationRequest() ? "Translation requested" : null);
        message.setHasCorrectionRequest(request.isCorrectionRequest());
        LanguageMessage updated = messageRepository.save(message);
        roomLiveUpdateService.publish(message.getRoomId(), "room-update", "message-updated");
        return updated;
    }

    @Override
    public LanguageMessage acceptTranslation(Long roomId, Long messageId, Long userId) {
        LanguageMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id " + messageId));
        if (!message.getRoomId().equals(roomId)) {
            throw new BadRequestException("Message does not belong to this room");
        }
        if (!message.getSenderId().equals(userId)) {
            throw new BadRequestException("You can only confirm translation for your own message");
        }
        if (!message.isTranslationRequest()) {
            return message;
        }
        boolean teacherReplyExists = messageRepository.findByRoomIdOrderBySentAtAsc(roomId).stream()
                .filter(candidate -> candidate.getSentAt().isAfter(message.getSentAt()))
                .anyMatch(candidate -> participantRepository.findByRoomIdAndUserId(roomId, candidate.getSenderId())
                        .map(participant -> participant.getRole() == ParticipantRole.HOST)
                        .orElse(false));
        if (!teacherReplyExists) {
            throw new BadRequestException("The teacher must reply before you can confirm the translation");
        }

        message.setTranslationRequest(false);
        message.setAutoTranslation("Translation seen");
        LanguageMessage updated = messageRepository.save(message);
        exchangeImpactService.onTranslationRequested(roomId, userId);
        roomLiveUpdateService.publish(roomId, "room-update", "translation-seen");
        return updated;
    }

    @Override
    public void delete(Long roomId, Long messageId, Long userId) {
        LanguageMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id " + messageId));
        if (!message.getRoomId().equals(roomId) || !message.getSenderId().equals(userId)) {
            throw new BadRequestException("You can only delete your own message");
        }
        messageRepository.delete(message);
        roomLiveUpdateService.publish(roomId, "room-update", "message-deleted");
    }
}
