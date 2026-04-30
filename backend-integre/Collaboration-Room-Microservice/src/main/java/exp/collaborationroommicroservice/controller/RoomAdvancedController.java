package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.dto.response.RoomAnalyticsResponse;
import exp.collaborationroommicroservice.dto.response.RoomMatchResponse;
import exp.collaborationroommicroservice.dto.response.ChallengeResponse;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.security.SecurityUtils;
import exp.collaborationroommicroservice.service.ChallengeGeneratorService;
import exp.collaborationroommicroservice.service.RoomAnalyticsService;
import exp.collaborationroommicroservice.service.RoomMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaboration/advanced")
@RequiredArgsConstructor
public class RoomAdvancedController {

    private final RoomMatchingService roomMatchingService;
    private final RoomAnalyticsService roomAnalyticsService;
    private final ChallengeGeneratorService challengeGeneratorService;
    private final CollaborationMapper collaborationMapper;

    @GetMapping("/rooms/matching/me")
    public List<RoomMatchResponse> getRecommendations() {
        return roomMatchingService.recommendForCurrentUser();
    }

    @GetMapping("/rooms/{roomId}/analytics")
    public RoomAnalyticsResponse getAnalytics(@PathVariable Long roomId) {
        return roomAnalyticsService.getAnalytics(roomId);
    }

    @PostMapping("/rooms/{roomId}/challenges/generate")
    public ResponseEntity<ChallengeResponse> generateChallenge(@PathVariable Long roomId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborationMapper.toResponse(
                        challengeGeneratorService.generateForRoom(roomId, SecurityUtils.getCurrentUserId())
                ));
    }
}
