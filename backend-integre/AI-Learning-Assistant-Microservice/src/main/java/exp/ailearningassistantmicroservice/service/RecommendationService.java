package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.entities.Recommendation;

import java.util.List;

public interface RecommendationService {

    List<Recommendation> generateForUser(Long userId);

    List<Recommendation> generateForAdmin(Long userId);

    List<Recommendation> getByUserId(Long userId);

    Recommendation getById(Long id);

    void delete(Long id);
}
