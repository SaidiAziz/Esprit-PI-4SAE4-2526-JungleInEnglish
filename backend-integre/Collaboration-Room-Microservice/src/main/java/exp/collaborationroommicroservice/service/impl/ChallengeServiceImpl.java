package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.ChallengeAnswerRequest;
import exp.collaborationroommicroservice.dto.ChallengeAnswerResponse;
import exp.collaborationroommicroservice.dto.CreateChallengeRequest;
import exp.collaborationroommicroservice.entity.*;
import exp.collaborationroommicroservice.exception.BadRequestException;
import exp.collaborationroommicroservice.exception.ResourceNotFoundException;
import exp.collaborationroommicroservice.integration.AiImpactService;
import exp.collaborationroommicroservice.repository.ChallengeSubmissionRepository;
import exp.collaborationroommicroservice.repository.LinguisticChallengeRepository;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.ActivityTrackingService;
import exp.collaborationroommicroservice.service.BadgeService;
import exp.collaborationroommicroservice.service.ChallengeService;
import exp.collaborationroommicroservice.service.ReputationService;
import exp.collaborationroommicroservice.service.RoomLiveUpdateService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final LinguisticChallengeRepository challengeRepository;
    private final ChallengeSubmissionRepository submissionRepository;
    private final RoomParticipantRepository participantRepository;
    private final RoomService roomService;
    private final BadgeService badgeService;
    private final AiImpactService aiImpactService;
    private final ReputationService reputationService;
    private final ActivityTrackingService activityTrackingService;
    private final RoomLiveUpdateService roomLiveUpdateService;

    @Override
    public LinguisticChallenge create(Long roomId, Long userId, CreateChallengeRequest request) {
        roomService.getById(roomId);
        LinguisticChallenge challenge = challengeRepository.save(LinguisticChallenge.builder()
                .roomId(roomId)
                .creatorId(userId)
                .type(request.getType())
                .prompt(request.getPrompt())
                .correctAnswer(request.getCorrectAnswer())
                .deadline(request.getDeadline())
                .difficulty(request.getDifficulty())
                .pointsReward(request.getPointsReward())
                .status(ChallengeStatus.OPEN)
                .build());
        activityTrackingService.track(userId);
        badgeService.evaluateMilestones(userId, roomId);
        roomLiveUpdateService.publish(roomId, "room-update", "challenge-created");
        return challenge;
    }

    @Override
    public List<LinguisticChallenge> getRoomChallenges(Long roomId) {
        roomService.getById(roomId);
        return challengeRepository.findByRoomIdOrderByDeadlineDesc(roomId);
    }

    @Override
    public LinguisticChallenge getById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found with id " + challengeId));
    }

    @Override
    public ChallengeAnswerResponse submitAnswer(Long challengeId, Long userId, ChallengeAnswerRequest request) {
        LinguisticChallenge challenge = getById(challengeId);
        if (challenge.getStatus() != ChallengeStatus.OPEN) {
            throw new BadRequestException("This challenge is not open");
        }
        RoomParticipant participant = participantRepository.findByRoomIdAndUserId(challenge.getRoomId(), userId)
                .orElseThrow(() -> new BadRequestException("Join the room before answering the challenge"));

        String expected = challenge.getCorrectAnswer();
        boolean correct = expected == null || expected.isBlank() || normalizeAnswer(expected).equals(normalizeAnswer(request.getAnswer()));
        int pointsAwarded = correct ? Math.max(challenge.getPointsReward(), 1) : 0;
        String feedback = correct ? "Challenge completed successfully" : "Incorrect answer, try again";
        ChallengeSubmission submission = submissionRepository.findByChallengeIdAndUserId(challengeId, userId)
                .orElseGet(() -> ChallengeSubmission.builder()
                        .challengeId(challengeId)
                        .userId(userId)
                        .build());
        boolean firstSuccessfulAttempt = correct && !submission.isCorrect();
        submission.setAnswer(request.getAnswer());
        submission.setCorrect(correct);
        submission.setFeedback(feedback);
        submission.setPointsAwarded(pointsAwarded);
        submissionRepository.save(submission);

        participant.setLastActiveAt(java.time.LocalDateTime.now());
        participantRepository.save(participant);

        if (firstSuccessfulAttempt) {
            CollaborationRoom room = roomService.getById(challenge.getRoomId());
            badgeService.awardIfMissing(userId, BadgeType.CHALLENGE_WINNER, room.getId());
            reputationService.onChallengeWon(room.getId(), userId);
            activityTrackingService.track(userId);
            badgeService.evaluateMilestones(userId, room.getId());
            aiImpactService.applyImpact(userId, room.getCourseId(), 0, 0, 10);
        }
        roomLiveUpdateService.publish(challenge.getRoomId(), "room-update", "challenge-answered");

        return ChallengeAnswerResponse.builder()
                .challengeId(challengeId)
                .userId(userId)
                .correct(correct)
                .feedback(feedback)
                .pointsAwarded(pointsAwarded)
                .build();
    }

    @Override
    public List<ChallengeSubmission> getSubmissions(Long challengeId, Long currentUserId) {
        LinguisticChallenge challenge = getById(challengeId);
        RoomParticipant participant = participantRepository.findByRoomIdAndUserId(challenge.getRoomId(), currentUserId)
                .orElseThrow(() -> new BadRequestException("Join the room before reviewing challenge answers"));
        if (!challenge.getCreatorId().equals(currentUserId) && participant.getRole() != ParticipantRole.HOST) {
            throw new BadRequestException("Only the tutor can review challenge answers");
        }
        return submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(challengeId);
    }

    @Override
    public LinguisticChallenge updateStatus(Long challengeId, Long currentUserId, ChallengeStatus status) {
        LinguisticChallenge challenge = getById(challengeId);
        if (!challenge.getCreatorId().equals(currentUserId)) {
            throw new BadRequestException("Only the challenge creator can change its status");
        }
        challenge.setStatus(status);
        LinguisticChallenge updated = challengeRepository.save(challenge);
        roomLiveUpdateService.publish(challenge.getRoomId(), "room-update", "challenge-status-updated");
        return updated;
    }

    @Override
    public void delete(Long challengeId, Long currentUserId) {
        LinguisticChallenge challenge = getById(challengeId);
        if (!challenge.getCreatorId().equals(currentUserId)) {
            throw new BadRequestException("Only the challenge creator can delete it");
        }
        Long roomId = challenge.getRoomId();
        challengeRepository.delete(challenge);
        roomLiveUpdateService.publish(roomId, "room-update", "challenge-deleted");
    }
    private String normalizeAnswer(String raw) {
        if (raw == null) return "";
        return raw.trim()
                  .toLowerCase()
                  .replaceAll("[.!?,;:'\"]+$", "")   // strip trailing punctuation
                  .replaceAll("\\s+", " ")            // collapse multiple spaces
                  .trim();
    }
}
