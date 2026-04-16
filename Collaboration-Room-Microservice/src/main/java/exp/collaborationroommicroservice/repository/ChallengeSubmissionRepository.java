package exp.collaborationroommicroservice.repository;

import exp.collaborationroommicroservice.entity.ChallengeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeSubmissionRepository extends JpaRepository<ChallengeSubmission, Long> {

    List<ChallengeSubmission> findByChallengeIdOrderBySubmittedAtDesc(Long challengeId);

    Optional<ChallengeSubmission> findByChallengeIdAndUserId(Long challengeId, Long userId);
}
