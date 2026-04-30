package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.dto.response.*;
import exp.collaborationroommicroservice.entity.RoomStatus;
import exp.collaborationroommicroservice.service.CollaborationAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collaboration/admin")
@RequiredArgsConstructor
public class CollaborationAdminController {

    private final CollaborationAdminService collaborationAdminService;

    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {
        return collaborationAdminService.getDashboard();
    }

    @GetMapping("/rooms")
    public List<AdminRoomOverviewResponse> getRooms() {
        return collaborationAdminService.getRoomOverviews();
    }

    @GetMapping("/rooms/{roomId}")
    public AdminRoomDetailResponse getRoom(@PathVariable Long roomId) {
        return collaborationAdminService.getRoomDetail(roomId);
    }

    @GetMapping("/rooms/{roomId}/participants")
    public List<ParticipantResponse> getRoomParticipants(@PathVariable Long roomId) {
        return collaborationAdminService.getRoomParticipants(roomId);
    }

    @PatchMapping("/rooms/{roomId}/status")
    public AdminRoomOverviewResponse updateRoomStatus(@PathVariable Long roomId, @RequestBody Map<String, String> payload) {
        return collaborationAdminService.updateRoomStatus(roomId, RoomStatus.valueOf(payload.getOrDefault("status", "ACTIVE")));
    }

    @DeleteMapping("/rooms/{roomId}/participants/{userId}")
    public void removeParticipant(@PathVariable Long roomId, @PathVariable Long userId) {
        collaborationAdminService.removeParticipant(roomId, userId);
    }

    @GetMapping("/activity")
    public List<AdminActivityItemResponse> getRecentActivity() {
        return collaborationAdminService.getRecentActivity();
    }
}
