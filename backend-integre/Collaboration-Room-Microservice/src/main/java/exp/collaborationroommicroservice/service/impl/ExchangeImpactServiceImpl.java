package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.entity.CollaborationRoom;
import exp.collaborationroommicroservice.integration.AiImpactService;
import exp.collaborationroommicroservice.service.ExchangeImpactService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ExchangeImpactServiceImpl implements ExchangeImpactService {

    private final RoomService roomService;
    private final AiImpactService aiImpactService;

    private final Map<String, LocalDateTime> activeCallSessions = new ConcurrentHashMap<>();

    @Override
    public void onTranslationRequested(Long roomId, Long userId) {
        CollaborationRoom room = roomService.getById(roomId);
        aiImpactService.applyImpact(userId, room.getCourseId(), 0, 2, 0);
    }

    @Override
    public void onCorrectionApproved(Long roomId, Long learnerUserId) {
        CollaborationRoom room = roomService.getById(roomId);
        aiImpactService.applyImpact(learnerUserId, room.getCourseId(), 3, 0, 0);
    }

    @Override
    public void onCallJoined(Long roomId, Long userId) {
        activeCallSessions.put(sessionKey(roomId, userId), LocalDateTime.now());
    }

    @Override
    public void onCallLeft(Long roomId, Long userId) {
        LocalDateTime joinedAt = activeCallSessions.remove(sessionKey(roomId, userId));
        if (joinedAt == null) {
            return;
        }

        long minutes = Math.max(1, Duration.between(joinedAt, LocalDateTime.now()).toMinutes());
        int speakingDelta = speakingDeltaForMinutes(minutes);
        if (speakingDelta <= 0) {
            return;
        }

        CollaborationRoom room = roomService.getById(roomId);
        aiImpactService.applyImpact(userId, room.getCourseId(), 0, 0, speakingDelta);
    }

    private int speakingDeltaForMinutes(long minutes) {
        if (minutes < 5) {
            return 0;
        }
        if (minutes < 10) {
            return 2;
        }
        if (minutes < 15) {
            return 4;
        }
        return 6;
    }

    private String sessionKey(Long roomId, Long userId) {
        return roomId + ":" + userId;
    }
}
