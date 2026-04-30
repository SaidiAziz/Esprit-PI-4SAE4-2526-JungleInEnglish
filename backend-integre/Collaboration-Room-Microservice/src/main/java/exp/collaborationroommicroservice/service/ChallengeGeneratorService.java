package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.entity.LinguisticChallenge;

public interface ChallengeGeneratorService {

    LinguisticChallenge generateForRoom(Long roomId, Long currentUserId);
}
