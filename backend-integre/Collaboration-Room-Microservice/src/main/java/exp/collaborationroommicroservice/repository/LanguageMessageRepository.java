package exp.collaborationroommicroservice.repository;

import exp.collaborationroommicroservice.entity.LanguageMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageMessageRepository extends JpaRepository<LanguageMessage, Long> {

    List<LanguageMessage> findByRoomIdOrderBySentAtAsc(Long roomId);

    long countBySenderId(Long senderId);
}
