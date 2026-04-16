package exp.ailearningassistantmicroservice.service.impl;

import exp.ailearningassistantmicroservice.dto.response.AiAdminDashboardResponse;
import exp.ailearningassistantmicroservice.dto.response.AiAdminMetricResponse;
import exp.ailearningassistantmicroservice.dto.response.AiAdminStudentOverviewResponse;
import exp.ailearningassistantmicroservice.dto.response.LearningPathResponse;
import exp.ailearningassistantmicroservice.dto.response.PerformanceAnalysisResponse;
import exp.ailearningassistantmicroservice.dto.response.RecommendationResponse;
import exp.ailearningassistantmicroservice.dto.response.UserAiSummaryResponse;
import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.mapper.AiMapper;
import exp.ailearningassistantmicroservice.repositories.LearningPathRepository;
import exp.ailearningassistantmicroservice.repositories.PerformanceAnalysisRepository;
import exp.ailearningassistantmicroservice.repositories.RecommendationRepository;
import exp.ailearningassistantmicroservice.service.AiAdminService;
import exp.ailearningassistantmicroservice.service.AiRuleEngine;
import exp.ailearningassistantmicroservice.service.AiSummaryService;
import exp.ailearningassistantmicroservice.service.LearningPathService;
import exp.ailearningassistantmicroservice.service.RecommendationResponseAssembler;
import exp.ailearningassistantmicroservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiAdminServiceImpl implements AiAdminService {

    private final PerformanceAnalysisRepository performanceAnalysisRepository;
    private final RecommendationRepository recommendationRepository;
    private final LearningPathRepository learningPathRepository;
    private final RecommendationService recommendationService;
    private final LearningPathService learningPathService;
    private final AiSummaryService aiSummaryService;
    private final AiRuleEngine aiRuleEngine;
    private final AiMapper aiMapper;
    private final RecommendationResponseAssembler recommendationResponseAssembler;

    @Override
    public AiAdminDashboardResponse getDashboard() {
        List<PerformanceAnalysis> analyses = performanceAnalysisRepository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdated"));
        List<PerformanceAnalysisResponse> recentAnalyses = analyses.stream()
                .limit(8)
                .map(aiMapper::toResponse)
                .toList();

        Map<String, Long> levelDistribution = analyses.stream()
                .collect(Collectors.groupingBy(analysis -> analysis.getLevel().name(), Collectors.counting()));

        Map<String, Long> weakSkillDistribution = analyses.stream()
                .collect(Collectors.groupingBy(
                        analysis -> aiRuleEngine.determineWeakestSkill(List.of(analysis)).name(),
                        Collectors.counting()
                ));

        return AiAdminDashboardResponse.builder()
                .totalStudents(analyses.stream().map(PerformanceAnalysis::getUserId).distinct().count())
                .totalAnalyses(analyses.size())
                .totalRecommendations(recommendationRepository.count())
                .totalLearningPaths(learningPathRepository.count())
                .levelDistribution(toMetricResponses(levelDistribution))
                .weakestSkillDistribution(toMetricResponses(weakSkillDistribution))
                .recentAnalyses(recentAnalyses)
                .build();
    }

    @Override
    public List<AiAdminStudentOverviewResponse> getStudents() {
        List<PerformanceAnalysis> analyses = performanceAnalysisRepository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdated"));
        Map<Long, List<PerformanceAnalysis>> analysesByUser = analyses.stream()
                .collect(Collectors.groupingBy(PerformanceAnalysis::getUserId));
        Map<Long, Long> recommendationCounts = recommendationRepository.findAll().stream()
                .collect(Collectors.groupingBy(recommendation -> recommendation.getUserId(), Collectors.counting()));
        Map<Long, Long> learningPathCounts = learningPathRepository.findAll().stream()
                .collect(Collectors.groupingBy(path -> path.getUserId(), Collectors.counting()));

        return analysesByUser.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<PerformanceAnalysis> studentAnalyses = entry.getValue();
                    double overallAverage = studentAnalyses.stream().mapToDouble(PerformanceAnalysis::getAverageScore).average().orElse(0);
                    return AiAdminStudentOverviewResponse.builder()
                            .userId(userId)
                            .currentLevel(aiRuleEngine.calculateLevel(overallAverage))
                            .overallAverage(Math.round(overallAverage * 100.0) / 100.0)
                            .weakestSkill(aiRuleEngine.determineWeakestSkill(studentAnalyses))
                            .analysisCount(studentAnalyses.size())
                            .recommendationCount(recommendationCounts.getOrDefault(userId, 0L).intValue())
                            .learningPathCount(learningPathCounts.getOrDefault(userId, 0L).intValue())
                            .lastAnalysisAt(studentAnalyses.stream()
                                    .map(PerformanceAnalysis::getLastUpdated)
                                    .max(Comparator.naturalOrder())
                                    .orElse(null))
                            .build();
                })
                .sorted(Comparator.comparing(AiAdminStudentOverviewResponse::getLastAnalysisAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public UserAiSummaryResponse getStudentDetail(Long userId) {
        return aiSummaryService.getUserSummary(userId);
    }

    @Override
    public List<RecommendationResponse> getRecommendations() {
        return recommendationResponseAssembler.toResponses(
                recommendationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    @Override
    public List<RecommendationResponse> regenerateRecommendations(Long userId) {
        return recommendationResponseAssembler.toResponses(recommendationService.generateForAdmin(userId));
    }

    @Override
    public List<LearningPathResponse> getLearningPaths() {
        return learningPathRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(aiMapper::toResponse)
                .toList();
    }

    @Override
    public LearningPathResponse regenerateLearningPath(Long userId, Long courseId) {
        return aiMapper.toResponse(learningPathService.generateOrUpdateForAdmin(userId, courseId));
    }

    private List<AiAdminMetricResponse> toMetricResponses(Map<String, Long> source) {
        return source.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> AiAdminMetricResponse.builder()
                        .label(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .toList();
    }
}
