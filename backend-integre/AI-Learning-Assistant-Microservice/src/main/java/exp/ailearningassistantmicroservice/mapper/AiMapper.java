package exp.ailearningassistantmicroservice.mapper;

import exp.ailearningassistantmicroservice.dto.response.LearningPathResponse;
import exp.ailearningassistantmicroservice.dto.response.PerformanceAnalysisResponse;
import exp.ailearningassistantmicroservice.dto.response.RecommendationResponse;
import exp.ailearningassistantmicroservice.entities.LearningPath;
import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.entities.Recommendation;
import exp.ailearningassistantmicroservice.integration.CourseCatalogItem;
import org.springframework.stereotype.Component;

@Component
public class AiMapper {

    public PerformanceAnalysisResponse toResponse(PerformanceAnalysis analysis) {
        return PerformanceAnalysisResponse.builder()
                .id(analysis.getId())
                .userId(analysis.getUserId())
                .courseId(analysis.getCourseId())
                .grammarScore(analysis.getGrammarScore())
                .listeningScore(analysis.getListeningScore())
                .speakingScore(analysis.getSpeakingScore())
                .averageScore(analysis.getAverageScore())
                .level(analysis.getLevel())
                .createdAt(analysis.getCreatedAt())
                .lastUpdated(analysis.getLastUpdated())
                .build();
    }

    public RecommendationResponse toResponse(Recommendation recommendation) {
        return toResponse(recommendation, null);
    }

    public RecommendationResponse toResponse(Recommendation recommendation, CourseCatalogItem course) {
        return RecommendationResponse.builder()
                .id(recommendation.getId())
                .userId(recommendation.getUserId())
                .type(recommendation.getType())
                .contentId(recommendation.getContentId())
                .contentTitle(course != null ? course.getTitle() : null)
                .contentDescription(course != null ? course.getDescription() : null)
                .contentLevel(course != null ? course.getLevel() : null)
                .contentPrice(course != null ? course.getPrice() : null)
                .reason(recommendation.getReason())
                .focusSkill(recommendation.getFocusSkill())
                .createdAt(recommendation.getCreatedAt())
                .build();
    }

    public LearningPathResponse toResponse(LearningPath learningPath) {
        return LearningPathResponse.builder()
                .id(learningPath.getId())
                .userId(learningPath.getUserId())
                .courseId(learningPath.getCourseId())
                .lessonOrder(learningPath.getLessonOrder())
                .progress(learningPath.getProgress())
                .focusSkill(learningPath.getFocusSkill())
                .targetLevel(learningPath.getTargetLevel())
                .createdAt(learningPath.getCreatedAt())
                .updatedAt(learningPath.getUpdatedAt())
                .build();
    }
}
