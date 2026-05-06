package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.dto.CreateRoomRequest;
import exp.collaborationroommicroservice.dto.UpdateRoomRequest;
import exp.collaborationroommicroservice.entity.CollaborationRoom;
import exp.collaborationroommicroservice.entity.RoomLevel;
import exp.collaborationroommicroservice.entity.RoomStatus;

import java.util.List;

public interface RoomService {

    CollaborationRoom create(CreateRoomRequest request, Long currentUserId);

    List<CollaborationRoom> getPublicRooms();

    CollaborationRoom getById(Long id);

    List<CollaborationRoom> getMyRooms(Long userId);

    List<CollaborationRoom> getByLanguage(String language);

    List<CollaborationRoom> getByLevel(RoomLevel level);

    CollaborationRoom update(Long id, Long currentUserId, UpdateRoomRequest request);

    CollaborationRoom updateStatus(Long id, Long currentUserId, RoomStatus status);

    void delete(Long id, Long currentUserId);
}
