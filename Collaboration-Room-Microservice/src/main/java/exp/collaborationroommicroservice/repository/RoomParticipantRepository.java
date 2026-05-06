package exp.collaborationroommicroservice.repository;

import exp.collaborationroommicroservice.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    List<RoomParticipant> findByRoomIdOrderByJoinedAtAsc(Long roomId);

    List<RoomParticipant> findByUserIdOrderByJoinedAtDesc(Long userId);

    Optional<RoomParticipant> findByRoomIdAndUserId(Long roomId, Long userId);

    long countByRoomId(Long roomId);
}
