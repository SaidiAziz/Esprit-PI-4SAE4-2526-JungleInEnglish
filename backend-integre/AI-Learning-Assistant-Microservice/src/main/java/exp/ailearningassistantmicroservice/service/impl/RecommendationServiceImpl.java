package exp.ailearningassistantmicroservice.service.impl;

import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.entities.Recommendation;
import exp.ailearningassistantmicroservice.entities.RecommendationType;
import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.exception.ResourceNotFoundException;
import exp.ailearningassistantmicroservice.integration.CourseCatalogItem;
import exp.ailearningassistantmicroservice.integration.UserProfileValidationService;
import exp.ailearningassistantmicroservice.repositories.RecommendationRepository;
import exp.ailearningassistantmicroservice.service.AiRuleEngine;
import exp.ailearningassistantmicroservice.service.CourseCatalogLookupService;
import exp.ailearningassistantmicroservice.service.PerformanceAnalysisService;
import exp.ailearningassistantmicroservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final PerformanceAnalysisService performanceAnalysisService;
    private final AiRuleEngine aiRuleEngine;
    private final UserProfileValidationService userProfileValidationService;
    private final CourseCatalogLookupService courseCatalogLookupService;

    @Override
    @Transactional
    public List<Recommendation> generateForUser(Long userId) {
        userProfileValidationService.validateCurrentStudentProfile();
        return generateRecommendations(userId);
    }

    @Override
    @Transactional
    public List<Recommendation> generateForAdmin(Long userId) {
        return generateRecommendations(userId);
    }

    private List<Recommendation> generateRecommendations(Long userId) {
        List<PerformanceAnalysis> analyses = performanceAnalysisService.getByUserId(userId);
        if (analyses.isEmpty()) {
            throw new ResourceNotFoundException("No performance analyses found for user " + userId);
        }

        recommendationRepository.deleteByUserId(userId);

        SkillType weakestSkill = aiRuleEngine.determineWeakestSkill(analyses);
        PerformanceAnalysis latest = analyses.get(0);
        List<Recommendation> recommendations = new ArrayList<>();

        List<CourseCatalogItem> recommendedCourses = courseCatalogLookupService.findRecommendedCourses(
                latest.getLevel().name(),
                weakestSkill,
                latest.getCourseId(),
                3
        );

        if (!recommendedCourses.isEmpty()) {
            recommendedCourses.forEach(course -> recommendations.add(
                    buildRecommendation(
                            userId,
                            weakestSkill,
                            course.getId(),
                            RecommendationType.COURSE,
                            courseReason(latest.getLevel().name(), weakestSkill, course)
                    )
            ));
        } else {
            recommendations.add(buildRecommendation(userId, weakestSkill, latest.getCourseId(), RecommendationType.COURSE,
                    "Recommended course aligned with your current " + latest.getLevel() + " level and weakest skill: " + weakestSkill));
            recommendations.add(buildRecommendation(userId, weakestSkill, latest.getCourseId() * 10 + 1, RecommendationType.LESSON,
                    lessonReason(weakestSkill)));
            recommendations.add(buildRecommendation(userId, weakestSkill, latest.getCourseId() * 100 + 1, RecommendationType.EXERCISE,
                    exerciseReason(weakestSkill)));
        }

        return recommendationRepository.saveAll(recommendations);
    }

    @Override
    public List<Recommendation> getByUserId(Long userId) {
        return recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Recommendation getById(Long id) {
        return recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with id " + id));
    }

    @Override
    public void delete(Long id) {
        recommendationRepository.delete(getById(id));
    }

    private Recommendation buildRecommendation(Long userId, SkillType weakestSkill, Long contentId, RecommendationType type, String reason) {
        return Recommendation.builder()
                .userId(userId)
                .type(type)
                .contentId(contentId)
                .reason(reason)
                .focusSkill(weakestSkill)
                .build();
    }

    private String courseReason(String level, SkillType weakestSkill, CourseCatalogItem course) {
        String courseTitle = course.getTitle() != null ? course.getTitle() : "this course";
        return "Recommended course \"" + courseTitle + "\" for your " + level + " level and " + weakestSkill + " focus.";
    }

    private String lessonReason(SkillType weakestSkill) {
        return switch (weakestSkill) {
            case GRAMMAR -> "Review a grammar-focused lesson to strengthen sentence structure and accuracy.";
            case LISTENING -> "Revisit a listening lesson to improve comprehension and audio recognition.";
            case SPEAKING -> "Practice a speaking lesson to improve fluency and pronunciation.";
        };
    }

    private String exerciseReason(SkillType weakestSkill) {
        return switch (weakestSkill) {
            case GRAMMAR -> "Targeted grammar exercises will help correct recurring rule mistakes.";
            case LISTENING -> "Short listening drills will reinforce your weakest comprehension area.";
            case SPEAKING -> "Guided speaking exercises will improve confidence and oral production.";
        };
    }
}
