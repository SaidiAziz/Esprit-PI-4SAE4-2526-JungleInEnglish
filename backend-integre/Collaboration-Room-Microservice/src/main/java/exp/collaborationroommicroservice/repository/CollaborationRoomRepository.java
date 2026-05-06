package exp.collaborationroommicroservice.repository;

import exp.collaborationroommicroservice.entity.CollaborationRoom;
import exp.collaborationroommicroservice.entity.RoomLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollaborationRoomRepository extends JpaRepository<CollaborationRoom, Long> {

    List<CollaborationRoom> findByIsPublicTrueOrderByCreatedAtDesc();

    List<CollaborationRoom> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    List<CollaborationRoom> findByTargetLanguageIgnoreCaseOrderByCreatedAtDesc(String targetLanguage);

    List<CollaborationRoom> findByLevelOrderByCreatedAtDesc(RoomLevel level);
}
