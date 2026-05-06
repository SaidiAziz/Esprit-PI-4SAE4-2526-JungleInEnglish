package exp.ailearningassistantmicroservice.repositories;

import exp.ailearningassistantmicroservice.entities.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
