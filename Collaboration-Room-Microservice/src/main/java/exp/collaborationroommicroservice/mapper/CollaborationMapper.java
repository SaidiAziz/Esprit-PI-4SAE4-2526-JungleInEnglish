package exp.collaborationroommicroservice.mapper;

import exp.collaborationroommicroservice.dto.response.*;
import exp.collaborationroommicroservice.entity.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CollaborationMapper {

    public RoomResponse toResponse(CollaborationRoom room) {
        return RoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .nativeLanguage(room.getNativeLanguage())
                .targetLanguage(room.getTargetLanguage())
                .level(room.getLevel())
                .type(room.getType())
                .maxParticipants(room.getMaxParticipants())
                .isPublic(room.isPublic())
                .topic(room.getTopic())
                .status(room.getStatus())
                .courseId(room.getCourseId())
                .createdBy(room.getCreatedBy())
                .createdAt(room.getCreatedAt())
                .build();
    }

    public ParticipantResponse toResponse(RoomParticipant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .roomId(participant.getRoomId())
                .userId(participant.getUserId())
                .role(participant.getRole())
                .joinedAt(participant.getJoinedAt())
                .lastActiveAt(participant.getLastActiveAt())
                .messagesCount(participant.getMessagesCount())
                .correctionsGiven(participant.getCorrectionsGiven())
                .correctionsReceived(participant.getCorrectionsReceived())
                .reputationScore(participant.getReputationScore())
                .build();
    }

    public MessageResponse toResponse(LanguageMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .language(message.getLanguage())
                .translationRequest(message.isTranslationRequest())
                .autoTranslation(message.getAutoTranslation())
                .correctionRequest(message.isHasCorrectionRequest())
                .mediaUrl(message.getMediaUrl())
                .sentAt(message.getSentAt())
                .build();
    }

    public CorrectionResponse toResponse(PeerCorrection correction) {
        return CorrectionResponse.builder()
                .id(correction.getId())
                .messageId(correction.getMessageId())
                .correctorId(correction.getCorrectorId())
                .originalText(correction.getOriginalText())
                .correctedText(correction.getCorrectedText())
                .explanation(correction.getExplanation())
                .errorType(correction.getErrorType())
                .accepted(correction.isAccepted())
                .helpfulVotes(correction.getHelpfulVotes())
                .correctedAt(correction.getCorrectedAt())
                .build();
    }

    public ChallengeResponse toResponse(LinguisticChallenge challenge) {
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .roomId(challenge.getRoomId())
                .creatorId(challenge.getCreatorId())
                .type(challenge.getType())
                .prompt(challenge.getPrompt())
                .correctAnswer(challenge.getCorrectAnswer())
                .deadline(challenge.getDeadline())
                .difficulty(challenge.getDifficulty())
                .pointsReward(challenge.getPointsReward())
                .status(challenge.getStatus())
                .build();
    }

    public ChallengeSubmissionResponse toResponse(ChallengeSubmission submission) {
        return ChallengeSubmissionResponse.builder()
                .id(submission.getId())
                .challengeId(submission.getChallengeId())
                .userId(submission.getUserId())
                .answer(submission.getAnswer())
                .correct(submission.isCorrect())
                .feedback(submission.getFeedback())
                .pointsAwarded(submission.getPointsAwarded())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    public BadgeResponse toResponse(CollaborationBadge badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .userId(badge.getUserId())
                .badgeType(badge.getBadgeType())
                .roomId(badge.getRoomId())
                .earnedAt(badge.getEarnedAt())
                .build();
    }

    public BadgeLeaderboardResponse toLeaderboardResponse(Map<String, Object> source) {
        return BadgeLeaderboardResponse.builder()
                .userId((Long) source.get("userId"))
                .badgeCount((Long) source.get("badgeCount"))
                .build();
    }
}
