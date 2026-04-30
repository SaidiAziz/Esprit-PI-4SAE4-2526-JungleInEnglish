package exp.ailearningassistantmicroservice.service.impl;

import exp.ailearningassistantmicroservice.dto.request.ApplyPerformanceImpactRequest;
import exp.ailearningassistantmicroservice.dto.request.CreatePerformanceAnalysisRequest;
import exp.ailearningassistantmicroservice.dto.request.UpdatePerformanceAnalysisRequest;
import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.exception.BadRequestException;
import exp.ailearningassistantmicroservice.exception.ResourceNotFoundException;
import exp.ailearningassistantmicroservice.integration.UserProfileValidationService;
import exp.ailearningassistantmicroservice.repositories.LearningPathRepository;
import exp.ailearningassistantmicroservice.repositories.PerformanceAnalysisRepository;
import exp.ailearningassistantmicroservice.service.AiRuleEngine;
import exp.ailearningassistantmicroservice.service.PerformanceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceAnalysisServiceImpl implements PerformanceAnalysisService {

    private final PerformanceAnalysisRepository performanceAnalysisRepository;
    private final LearningPathRepository learningPathRepository;
    private final AiRuleEngine aiRuleEngine;
    private final UserProfileValidationService userProfileValidationService;

    @Override
    public PerformanceAnalysis create(CreatePerformanceAnalysisRequest request) {
        userProfileValidationService.validateCurrentStudentProfile();
        performanceAnalysisRepository.findByUserIdAndCourseId(request.getUserId(), request.getCourseId())
                .ifPresent(existing -> {
                    throw new BadRequestException("Performance analysis already exists for this user and course");
                });

        PerformanceAnalysis analysis = PerformanceAnalysis.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .grammarScore(request.getGrammarScore())
                .listeningScore(request.getListeningScore())
                .speakingScore(request.getSpeakingScore())
                .build();

        applyComputedFields(analysis);
        return performanceAnalysisRepository.save(analysis);
    }

    @Override
    public PerformanceAnalysis applyImpact(ApplyPerformanceImpactRequest request) {
        userProfileValidationService.validateCurrentStudentProfile();
        PerformanceAnalysis analysis = performanceAnalysisRepository.findByUserIdAndCourseId(request.getUserId(), request.getCourseId())
                .orElseGet(() -> PerformanceAnalysis.builder()
                        .userId(request.getUserId())
                        .courseId(request.getCourseId())
                        .grammarScore(0)
                        .listeningScore(0)
                        .speakingScore(0)
                        .build());

        analysis.setGrammarScore(clampScore(analysis.getGrammarScore() + request.getGrammarDelta()));
        analysis.setListeningScore(clampScore(analysis.getListeningScore() + request.getListeningDelta()));
        analysis.setSpeakingScore(clampScore(analysis.getSpeakingScore() + request.getSpeakingDelta()));
        applyComputedFields(analysis);
        
        PerformanceAnalysis savedAnalysis = performanceAnalysisRepository.save(analysis);
        
        learningPathRepository.findByUserIdAndCourseId(request.getUserId(), request.getCourseId())
                .ifPresent(path -> {
                    path.setProgress(Math.round(savedAnalysis.getAverageScore() * 100.0) / 100.0);
                    learningPathRepository.save(path);
                });
                
        return savedAnalysis;
    }

    @Override
    public PerformanceAnalysis update(Long id, Long userId, UpdatePerformanceAnalysisRequest request) {
        PerformanceAnalysis analysis = getById(id);
        validateOwnership(analysis, userId);
        performanceAnalysisRepository.findByUserIdAndCourseId(analysis.getUserId(), request.getCourseId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Another performance analysis already exists for this user and course");
                });
        analysis.setCourseId(request.getCourseId());
        analysis.setGrammarScore(request.getGrammarScore());
        analysis.setListeningScore(request.getListeningScore());
        analysis.setSpeakingScore(request.getSpeakingScore());
        applyComputedFields(analysis);
        return performanceAnalysisRepository.save(analysis);
    }

    @Override
    public PerformanceAnalysis getById(Long id) {
        return performanceAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance analysis not found with id " + id));
    }

    @Override
    public List<PerformanceAnalysis> getAll() {
        return performanceAnalysisRepository.findAll();
    }

    @Override
    public List<PerformanceAnalysis> getByUserId(Long userId) {
        return performanceAnalysisRepository.findByUserIdOrderByLastUpdatedDesc(userId);
    }

    @Override
    public List<PerformanceAnalysis> getByCourseId(Long courseId) {
        return performanceAnalysisRepository.findByCourseIdOrderByLastUpdatedDesc(courseId);
    }

    @Override
    public PerformanceAnalysis getByUserIdAndCourseId(Long userId, Long courseId) {
        return performanceAnalysisRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance analysis not found for user " + userId + " and course " + courseId));
    }

    @Override
    public void delete(Long id, Long userId) {
        PerformanceAnalysis analysis = getById(id);
        validateOwnership(analysis, userId);
        performanceAnalysisRepository.delete(analysis);
    }

    private void applyComputedFields(PerformanceAnalysis analysis) {
        double average = aiRuleEngine.calculateAverage(
                analysis.getGrammarScore(),
                analysis.getListeningScore(),
                analysis.getSpeakingScore()
        );
        analysis.setAverageScore(average);
        analysis.setLevel(aiRuleEngine.calculateLevel(average));
    }

    private void validateOwnership(PerformanceAnalysis analysis, Long userId) {
        if (userId != null && !analysis.getUserId().equals(userId)) {
            throw new BadRequestException("You can only modify your own performance analyses");
        }
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
