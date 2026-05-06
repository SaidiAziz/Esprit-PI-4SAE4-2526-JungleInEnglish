package exp.ailearningassistantmicroservice.service.impl;

import exp.ailearningassistantmicroservice.entities.LearningPath;
import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.exception.BadRequestException;
import exp.ailearningassistantmicroservice.exception.ResourceNotFoundException;
import exp.ailearningassistantmicroservice.integration.UserProfileValidationService;
import exp.ailearningassistantmicroservice.repositories.LearningPathRepository;
import exp.ailearningassistantmicroservice.service.AiRuleEngine;
import exp.ailearningassistantmicroservice.service.LearningPathService;
import exp.ailearningassistantmicroservice.service.PerformanceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningPathServiceImpl implements LearningPathService {

    private final LearningPathRepository learningPathRepository;
    private final PerformanceAnalysisService performanceAnalysisService;
    private final AiRuleEngine aiRuleEngine;
    private final UserProfileValidationService userProfileValidationService;

    @Override
    public LearningPath generateOrUpdate(Long userId, Long courseId) {
        userProfileValidationService.validateCurrentStudentProfile();
        return buildLearningPath(userId, courseId);
    }

    @Override
    public LearningPath generateOrUpdateForAdmin(Long userId, Long courseId) {
        return buildLearningPath(userId, courseId);
    }

    private LearningPath buildLearningPath(Long userId, Long courseId) {
        PerformanceAnalysis analysis = performanceAnalysisService.getByUserIdAndCourseId(userId, courseId);
        SkillType weakestSkill = aiRuleEngine.determineWeakestSkill(
                analysis.getGrammarScore(),
                analysis.getListeningScore(),
                analysis.getSpeakingScore()
        );

        LearningPath learningPath = learningPathRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> LearningPath.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .progress(0)
                        .build());

        learningPath.setFocusSkill(weakestSkill);
        learningPath.setTargetLevel(analysis.getLevel());
        learningPath.setLessonOrder(aiRuleEngine.buildLessonOrder(weakestSkill, analysis.getLevel()));
        return learningPathRepository.save(learningPath);
    }

    @Override
    public LearningPath updateProgress(Long id, Long userId, double progress) {
        LearningPath learningPath = getById(id);
        validateOwnership(learningPath, userId);
        if (progress < 0 || progress > 100) {
            throw new BadRequestException("Progress must be between 0 and 100");
        }
        learningPath.setProgress(progress);
        return learningPathRepository.save(learningPath);
    }

    @Override
    public LearningPath getById(Long id) {
        return learningPathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id " + id));
    }

    @Override
    public List<LearningPath> getByUserId(Long userId) {
        return learningPathRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Override
    public LearningPath getByUserIdAndCourseId(Long userId, Long courseId) {
        return learningPathRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found for user " + userId + " and course " + courseId));
    }

    @Override
    public void delete(Long id, Long userId) {
        LearningPath learningPath = getById(id);
        validateOwnership(learningPath, userId);
        learningPathRepository.delete(learningPath);
    }

    private void validateOwnership(LearningPath learningPath, Long userId) {
        if (userId != null && !learningPath.getUserId().equals(userId)) {
            throw new BadRequestException("You can only modify your own learning paths");
        }
    }
}
