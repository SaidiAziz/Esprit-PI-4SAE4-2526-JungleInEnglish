package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.entity.BadgeType;
import exp.collaborationroommicroservice.entity.CollaborationBadge;

import java.util.List;
import java.util.Map;

public interface BadgeService {

    CollaborationBadge awardIfMissing(Long userId, BadgeType badgeType, Long roomId);

    List<CollaborationBadge> getUserBadges(Long userId);

    CollaborationBadge getById(Long badgeId);

    List<Map<String, Object>> getLeaderboard();

    void revoke(Long badgeId);

    void evaluateMilestones(Long userId, Long roomId);
}
