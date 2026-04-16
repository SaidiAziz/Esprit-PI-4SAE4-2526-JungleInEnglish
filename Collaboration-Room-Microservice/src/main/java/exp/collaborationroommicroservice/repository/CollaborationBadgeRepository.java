package exp.collaborationroommicroservice.repository;

import exp.collaborationroommicroservice.entity.BadgeType;
import exp.collaborationroommicroservice.entity.CollaborationBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollaborationBadgeRepository extends JpaRepository<CollaborationBadge, Long> {

    List<CollaborationBadge> findByUserIdOrderByEarnedAtDesc(Long userId);

    Optional<CollaborationBadge> findByUserIdAndBadgeTypeAndRoomId(Long userId, BadgeType badgeType, Long roomId);

    long countByUserId(Long userId);
}
