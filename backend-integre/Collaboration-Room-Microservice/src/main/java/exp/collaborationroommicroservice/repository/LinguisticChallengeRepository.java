package exp.collaborationroommicroservice.repository;

import exp.collaborationroommicroservice.entity.LinguisticChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinguisticChallengeRepository extends JpaRepository<LinguisticChallenge, Long> {

    List<LinguisticChallenge> findByRoomIdOrderByDeadlineDesc(Long roomId);
}
