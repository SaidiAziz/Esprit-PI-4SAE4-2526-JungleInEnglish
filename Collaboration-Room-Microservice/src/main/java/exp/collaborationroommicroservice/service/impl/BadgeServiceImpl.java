package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.entity.BadgeType;
import exp.collaborationroommicroservice.entity.CollaborationBadge;
import exp.collaborationroommicroservice.repository.CollaborationRoomRepository;
import exp.collaborationroommicroservice.exception.ResourceNotFoundException;
import exp.collaborationroommicroservice.repository.CollaborationBadgeRepository;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.ActivityTrackingService;
import exp.collaborationroommicroservice.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final CollaborationBadgeRepository badgeRepository;
    private final RoomParticipantRepository participantRepository;
    private final CollaborationRoomRepository roomRepository;
    private final ActivityTrackingService activityTrackingService;

    @Override
    public CollaborationBadge awardIfMissing(Long userId, BadgeType badgeType, Long roomId) {
        return badgeRepository.findByUserIdAndBadgeTypeAndRoomId(userId, badgeType, roomId)
                .orElseGet(() -> badgeRepository.save(CollaborationBadge.builder()
                        .userId(userId)
                        .badgeType(badgeType)
                        .roomId(roomId)
                        .build()));
    }

    @Override
    public List<CollaborationBadge> getUserBadges(Long userId) {
        return badgeRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }

    @Override
    public CollaborationBadge getById(Long badgeId) {
        return badgeRepository.findById(badgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found with id " + badgeId));
    }

    @Override
    public List<Map<String, Object>> getLeaderboard() {
        return badgeRepository.findAll().stream()
                .collect(Collectors.groupingBy(CollaborationBadge::getUserId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("userId", entry.getKey());
                    row.put("badgeCount", entry.getValue());
                    return row;
                })
                .toList();
    }

    @Override
    public void revoke(Long badgeId) {
        badgeRepository.delete(getById(badgeId));
    }

    @Override
    public void evaluateMilestones(Long userId, Long roomId) {
        long distinctLanguages = participantRepository.findByUserIdOrderByJoinedAtDesc(userId).stream()
                .map(participant -> roomRepository.findById(participant.getRoomId()).orElse(null))
                .filter(room -> room != null)
                .map(room -> room.getTargetLanguage().toLowerCase())
                .distinct()
                .count();

        if (distinctLanguages >= 3) {
            awardIfMissing(userId, BadgeType.POLYGLOT, null);
        }

        if (activityTrackingService.hasSevenDayStreak(userId)) {
            awardIfMissing(userId, BadgeType.STREAK_7DAYS, null);
        }
    }
}
