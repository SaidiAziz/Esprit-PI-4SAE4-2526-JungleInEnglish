package exp.ailearningassistantmicroservice.repositories;

import exp.ailearningassistantmicroservice.entities.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    List<LearningPath> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<LearningPath> findByUserIdAndCourseId(Long userId, Long courseId);
}
