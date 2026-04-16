package exp.collaborationroommicroservice.service;

import exp.collaborationroommicroservice.entity.RoomParticipant;
import exp.collaborationroommicroservice.entity.ParticipantRole;

import java.util.List;

public interface ParticipantService {

    RoomParticipant join(Long roomId, Long userId);

    void leave(Long roomId, Long userId);

    List<RoomParticipant> getRoomParticipants(Long roomId);

    RoomParticipant getParticipant(Long roomId, Long userId);

    RoomParticipant changeRole(Long roomId, Long targetUserId, Long actingUserId, ParticipantRole role);

    void kick(Long roomId, Long targetUserId, Long actingUserId);
}
