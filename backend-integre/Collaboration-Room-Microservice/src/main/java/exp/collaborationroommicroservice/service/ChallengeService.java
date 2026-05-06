package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.dto.ChallengeAnswerRequest;
import exp.collaborationroommicroservice.dto.ChallengeAnswerResponse;
import exp.collaborationroommicroservice.dto.CreateChallengeRequest;
import exp.collaborationroommicroservice.entity.ChallengeSubmission;
import exp.collaborationroommicroservice.entity.ChallengeStatus;
import exp.collaborationroommicroservice.entity.LinguisticChallenge;

import java.util.List;

public interface ChallengeService {

    LinguisticChallenge create(Long roomId, Long userId, CreateChallengeRequest request);

    List<LinguisticChallenge> getRoomChallenges(Long roomId);

    LinguisticChallenge getById(Long challengeId);

    ChallengeAnswerResponse submitAnswer(Long challengeId, Long userId, ChallengeAnswerRequest request);

    List<ChallengeSubmission> getSubmissions(Long challengeId, Long currentUserId);

    LinguisticChallenge updateStatus(Long challengeId, Long currentUserId, ChallengeStatus status);

    void delete(Long challengeId, Long currentUserId);
}
