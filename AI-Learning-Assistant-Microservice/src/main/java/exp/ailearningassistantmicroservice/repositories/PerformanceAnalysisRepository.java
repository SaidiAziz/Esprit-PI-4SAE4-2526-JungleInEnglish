package exp.ailearningassistantmicroservice.repositories;

import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerformanceAnalysisRepository extends JpaRepository<PerformanceAnalysis, Long> {

    List<PerformanceAnalysis> findByUserIdOrderByLastUpdatedDesc(Long userId);

    List<PerformanceAnalysis> findByCourseIdOrderByLastUpdatedDesc(Long courseId);

    Optional<PerformanceAnalysis> findByUserIdAndCourseId(Long userId, Long courseId);
}
