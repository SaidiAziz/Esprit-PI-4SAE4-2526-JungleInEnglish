package exp.ailearningassistantmicroservice.service.impl;

import exp.ailearningassistantmicroservice.dto.response.UserAiSummaryResponse;
import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.entities.StudentLevel;
import exp.ailearningassistantmicroservice.mapper.AiMapper;
import exp.ailearningassistantmicroservice.service.AiRuleEngine;
import exp.ailearningassistantmicroservice.service.AiSummaryService;
import exp.ailearningassistantmicroservice.service.LearningPathService;
import exp.ailearningassistantmicroservice.service.PerformanceAnalysisService;
import exp.ailearningassistantmicroservice.service.RecommendationResponseAssembler;
import exp.ailearningassistantmicroservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSummaryServiceImpl implements AiSummaryService {

    private final PerformanceAnalysisService performanceAnalysisService;
    private final RecommendationService recommendationService;
    private final LearningPathService learningPathService;
    private final AiRuleEngine aiRuleEngine;
    private final AiMapper aiMapper;
    private final RecommendationResponseAssembler recommendationResponseAssembler;

    @Override
    public UserAiSummaryResponse getUserSummary(Long userId) {
        List<PerformanceAnalysis> analyses = performanceAnalysisService.getByUserId(userId);
        double overallAverage = analyses.stream().mapToDouble(PerformanceAnalysis::getAverageScore).average().orElse(0);
        StudentLevel currentLevel = aiRuleEngine.calculateLevel(overallAverage);
        SkillType weakestSkill = aiRuleEngine.determineWeakestSkill(analyses);

        return UserAiSummaryResponse.builder()
                .userId(userId)
                .currentLevel(currentLevel)
                .overallAverage(Math.round(overallAverage * 100.0) / 100.0)
                .weakestSkill(weakestSkill)
                .totalAnalyses(analyses.size())
                .performanceAnalyses(analyses.stream().map(aiMapper::toResponse).toList())
                .recommendations(recommendationResponseAssembler.toResponses(recommendationService.getByUserId(userId)))
                .learningPaths(learningPathService.getByUserId(userId).stream().map(aiMapper::toResponse).toList())
                .build();
    }
}
