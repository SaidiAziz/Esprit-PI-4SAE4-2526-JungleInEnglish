package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.dto.response.AdminActivityItemResponse;
import exp.collaborationroommicroservice.dto.response.AdminDashboardResponse;
import exp.collaborationroommicroservice.dto.response.AdminRoomDetailResponse;
import exp.collaborationroommicroservice.dto.response.AdminRoomOverviewResponse;
import exp.collaborationroommicroservice.dto.response.ParticipantResponse;
import exp.collaborationroommicroservice.entity.RoomStatus;

import java.util.List;

public interface CollaborationAdminService {

    AdminDashboardResponse getDashboard();

    List<AdminRoomOverviewResponse> getRoomOverviews();

    AdminRoomDetailResponse getRoomDetail(Long roomId);

    List<ParticipantResponse> getRoomParticipants(Long roomId);

    AdminRoomOverviewResponse updateRoomStatus(Long roomId, RoomStatus status);

    void removeParticipant(Long roomId, Long userId);

    List<AdminActivityItemResponse> getRecentActivity();
}
