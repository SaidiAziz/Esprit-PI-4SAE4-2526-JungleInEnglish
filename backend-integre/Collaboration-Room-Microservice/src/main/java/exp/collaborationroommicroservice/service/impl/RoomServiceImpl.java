package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.dto.CreateRoomRequest;
import exp.collaborationroommicroservice.dto.UpdateRoomRequest;
import exp.collaborationroommicroservice.entity.*;
import exp.collaborationroommicroservice.exception.BadRequestException;
import exp.collaborationroommicroservice.exception.ResourceNotFoundException;
import exp.collaborationroommicroservice.repository.CollaborationRoomRepository;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.ActivityTrackingService;
import exp.collaborationroommicroservice.service.BadgeService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final CollaborationRoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final BadgeService badgeService;
    private final ActivityTrackingService activityTrackingService;

    @Override
    public CollaborationRoom create(CreateRoomRequest request, Long currentUserId) {
        CollaborationRoom room = roomRepository.save(CollaborationRoom.builder()
                .title(request.getTitle())
                .nativeLanguage(request.getNativeLanguage())
                .targetLanguage(request.getTargetLanguage())
                .level(request.getLevel())
                .type(request.getType())
                .maxParticipants(request.getMaxParticipants())
                .isPublic(request.isPublic())
                .topic(request.getTopic())
                .status(RoomStatus.ACTIVE)
                .courseId(request.getCourseId())
                .createdBy(currentUserId)
                .build());
        participantRepository.save(RoomParticipant.builder()
                .roomId(room.getId())
                .userId(currentUserId)
                .role(ParticipantRole.HOST)
                .build());
        activityTrackingService.track(currentUserId);
        badgeService.awardIfMissing(currentUserId, BadgeType.ROOM_CREATOR, room.getId());
        badgeService.evaluateMilestones(currentUserId, room.getId());
        return room;
    }

    @Override
    public List<CollaborationRoom> getPublicRooms() {
        return roomRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @Override
    public CollaborationRoom getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));
    }

    @Override
    public List<CollaborationRoom> getMyRooms(Long userId) {
        return roomRepository.findByCreatedByOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<CollaborationRoom> getByLanguage(String language) {
        return roomRepository.findByTargetLanguageIgnoreCaseOrderByCreatedAtDesc(language);
    }

    @Override
    public List<CollaborationRoom> getByLevel(RoomLevel level) {
        return roomRepository.findByLevelOrderByCreatedAtDesc(level);
    }

    @Override
    public CollaborationRoom update(Long id, Long currentUserId, UpdateRoomRequest request) {
        CollaborationRoom room = getById(id);
        validateOwner(room, currentUserId);
        room.setTitle(request.getTitle());
        room.setNativeLanguage(request.getNativeLanguage());
        room.setTargetLanguage(request.getTargetLanguage());
        room.setLevel(request.getLevel());
        room.setType(request.getType());
        room.setMaxParticipants(request.getMaxParticipants());
        room.setPublic(request.isPublic());
        room.setTopic(request.getTopic());
        room.setCourseId(request.getCourseId());
        return roomRepository.save(room);
    }

    @Override
    public CollaborationRoom updateStatus(Long id, Long currentUserId, RoomStatus status) {
        CollaborationRoom room = getById(id);
        validateOwner(room, currentUserId);
        room.setStatus(status);
        return roomRepository.save(room);
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        CollaborationRoom room = getById(id);
        validateOwner(room, currentUserId);
        roomRepository.delete(room);
    }

    private void validateOwner(CollaborationRoom room, Long currentUserId) {
        if (!room.getCreatedBy().equals(currentUserId)) {
            throw new BadRequestException("Only the room creator can modify this room");
        }
    }
}
