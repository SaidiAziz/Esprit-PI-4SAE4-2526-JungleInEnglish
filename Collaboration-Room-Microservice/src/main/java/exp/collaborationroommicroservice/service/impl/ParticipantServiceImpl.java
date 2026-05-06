package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.entity.*;
import exp.collaborationroommicroservice.exception.BadRequestException;
import exp.collaborationroommicroservice.exception.ResourceNotFoundException;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.repository.CollaborationRoomRepository;
import exp.collaborationroommicroservice.service.ActivityTrackingService;
import exp.collaborationroommicroservice.service.BadgeService;
import exp.collaborationroommicroservice.service.ParticipantService;
import exp.collaborationroommicroservice.service.RoomLiveUpdateService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final RoomParticipantRepository participantRepository;
    private final RoomService roomService;
    private final CollaborationRoomRepository roomRepository;
    private final ActivityTrackingService activityTrackingService;
    private final BadgeService badgeService;
    private final RoomLiveUpdateService roomLiveUpdateService;

    @Override
    public RoomParticipant join(Long roomId, Long userId) {
        participantRepository.findByRoomIdAndUserId(roomId, userId)
                .ifPresent(existing -> {
                    throw new BadRequestException("User is already in this room");
                });

        CollaborationRoom room = roomService.getById(roomId);
        if (room.getStatus() == RoomStatus.CLOSED) {
            throw new BadRequestException("This room is closed");
        }
        if (participantRepository.countByRoomId(roomId) >= room.getMaxParticipants()) {
            room.setStatus(RoomStatus.FULL);
            roomRepository.save(room);
            throw new BadRequestException("This room is already full");
        }

        ParticipantRole role = room.getCreatedBy().equals(userId) ? ParticipantRole.HOST : ParticipantRole.LEARNER;
        RoomParticipant participant = participantRepository.save(RoomParticipant.builder()
                .roomId(roomId)
                .userId(userId)
                .role(role)
                .build());
        activityTrackingService.track(userId);
        badgeService.evaluateMilestones(userId, roomId);
        roomLiveUpdateService.publish(roomId, "room-update", "participant-joined");
        return participant;
    }

    @Override
    public void leave(Long roomId, Long userId) {
        RoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BadRequestException("Participant not found in room"));
        participantRepository.delete(participant);
        CollaborationRoom room = roomService.getById(roomId);
        if (room.getStatus() == RoomStatus.FULL && participantRepository.countByRoomId(roomId) < room.getMaxParticipants()) {
            room.setStatus(RoomStatus.ACTIVE);
            roomRepository.save(room);
        }
        roomLiveUpdateService.publish(roomId, "room-update", "participant-left");
    }

    @Override
    public List<RoomParticipant> getRoomParticipants(Long roomId) {
        return participantRepository.findByRoomIdOrderByJoinedAtAsc(roomId);
    }

    @Override
    public RoomParticipant getParticipant(Long roomId, Long userId) {
        return participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found in room"));
    }

    @Override
    public RoomParticipant changeRole(Long roomId, Long targetUserId, Long actingUserId, ParticipantRole role) {
        validateHost(roomId, actingUserId);
        RoomParticipant participant = getParticipant(roomId, targetUserId);
        participant.setRole(role);
        return participantRepository.save(participant);
    }

    @Override
    public void kick(Long roomId, Long targetUserId, Long actingUserId) {
        validateHost(roomId, actingUserId);
        participantRepository.delete(getParticipant(roomId, targetUserId));
        roomLiveUpdateService.publish(roomId, "room-update", "participant-removed");
    }

    private void validateHost(Long roomId, Long actingUserId) {
        RoomParticipant actingParticipant = getParticipant(roomId, actingUserId);
        if (actingParticipant.getRole() != ParticipantRole.HOST) {
            throw new BadRequestException("Only the host can manage room participants");
        }
    }
}
