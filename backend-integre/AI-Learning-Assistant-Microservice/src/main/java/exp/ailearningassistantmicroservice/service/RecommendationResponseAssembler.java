package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.dto.response.RecommendationResponse;
import exp.ailearningassistantmicroservice.entities.Recommendation;
import exp.ailearningassistantmicroservice.integration.CourseCatalogItem;
import exp.ailearningassistantmicroservice.mapper.AiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RecommendationResponseAssembler {

    private final AiMapper aiMapper;
    private final CourseCatalogLookupService courseCatalogLookupService;

    public List<RecommendationResponse> toResponses(List<Recommendation> recommendations) {
        Map<Long, CourseCatalogItem> courseMap = courseCatalogLookupService.getCourseMap(
                recommendations.stream().map(Recommendation::getContentId).toList()
        );
        return recommendations.stream()
                .map(recommendation -> aiMapper.toResponse(recommendation, courseMap.get(recommendation.getContentId())))
                .toList();
    }

    public RecommendationResponse toResponse(Recommendation recommendation) {
        return aiMapper.toResponse(
                recommendation,
                courseCatalogLookupService.getCourseMap(List.of(recommendation.getContentId())).get(recommendation.getContentId())
        );
    }
}
