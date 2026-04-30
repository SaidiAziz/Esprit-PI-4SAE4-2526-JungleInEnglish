package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.dto.response.BadgeLeaderboardResponse;
import exp.collaborationroommicroservice.dto.response.BadgeResponse;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.security.SecurityUtils;
import exp.collaborationroommicroservice.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/collaboration/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final CollaborationMapper collaborationMapper;

    @GetMapping("/me")
    public List<BadgeResponse> getMine() {
        return badgeService.getUserBadges(SecurityUtils.getCurrentUserId()).stream().map(collaborationMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public BadgeResponse getById(@PathVariable Long id) {
        return collaborationMapper.toResponse(badgeService.getById(id));
    }

    @GetMapping("/leaderboard")
    public List<BadgeLeaderboardResponse> getLeaderboard() {
        return badgeService.getLeaderboard().stream().map(collaborationMapper::toLeaderboardResponse).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        badgeService.revoke(id);
        return ResponseEntity.noContent().build();
    }
}
