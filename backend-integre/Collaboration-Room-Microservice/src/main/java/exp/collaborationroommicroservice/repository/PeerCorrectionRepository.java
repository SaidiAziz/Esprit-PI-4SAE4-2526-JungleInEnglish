package exp.collaborationroommicroservice.repository;

import exp.collaborationroommicroservice.entity.PeerCorrection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeerCorrectionRepository extends JpaRepository<PeerCorrection, Long> {

    List<PeerCorrection> findByMessageIdOrderByCorrectedAtDesc(Long messageId);

    long countByCorrectorIdAndAcceptedTrue(Long correctorId);
}
