package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.response.RoomMatchResponse;
import exp.collaborationroommicroservice.entity.CollaborationRoom;
import exp.collaborationroommicroservice.entity.RoomLevel;
import exp.collaborationroommicroservice.entity.RoomType;
import exp.collaborationroommicroservice.integration.AiSummaryClient;
import exp.collaborationroommicroservice.integration.dto.AiSummaryClientResponse;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.service.RoomMatchingService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomMatchingServiceImpl implements RoomMatchingService {

    private final RoomService roomService;
    private final AiSummaryClient aiSummaryClient;
    private final CollaborationMapper collaborationMapper;

    @Override
    public List<RoomMatchResponse> recommendForCurrentUser() {
        AiSummaryClientResponse summary = aiSummaryClient.getCurrentSummary();
        RoomLevel preferredLevel = mapLevel(summary.getCurrentLevel());
        String weakestSkill = summary.getWeakestSkill() == null ? "" : summary.getWeakestSkill();

        return roomService.getPublicRooms().stream()
                .map(room -> toMatch(room, preferredLevel, weakestSkill))
                .sorted(Comparator.comparingInt(RoomMatchResponse::getPriorityScore).reversed())
                .limit(5)
                .toList();
    }

    private RoomMatchResponse toMatch(CollaborationRoom room, RoomLevel preferredLevel, String weakestSkill) {
        int score = 0;
        String reason = "Recommended for your current level.";

        if (room.getLevel() == preferredLevel) {
            score += 40;
        }
        if ("SPEAKING".equalsIgnoreCase(weakestSkill) && room.getType() == RoomType.VOICE) {
            score += 35;
            reason = "Speaking is your weak point, so voice rooms are prioritized.";
        } else if ("GRAMMAR".equalsIgnoreCase(weakestSkill) && room.getType() == RoomType.TEXT_CHAT) {
            score += 30;
            reason = "Grammar practice fits well with text-based collaboration.";
        } else if ("LISTENING".equalsIgnoreCase(weakestSkill) && room.getType() != RoomType.TEXT_CHAT) {
            score += 30;
            reason = "Listening improves faster in voice or mixed rooms.";
        }
        if (room.isPublic()) {
            score += 10;
        }
        if (room.getStatus().name().equals("ACTIVE")) {
            score += 10;
        }

        return RoomMatchResponse.builder()
                .room(collaborationMapper.toResponse(room))
                .recommendationReason(reason)
                .priorityScore(score)
                .build();
    }

    private RoomLevel mapLevel(String level) {
        if (level == null) {
            return RoomLevel.BEGINNER;
        }
        return switch (level.toUpperCase()) {
            case "ADVANCED" -> RoomLevel.ADVANCED;
            case "INTERMEDIATE" -> RoomLevel.INTERMEDIATE;
            default -> RoomLevel.BEGINNER;
        };
    }
}
