package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.dto.request.ApplyPerformanceImpactRequest;
import exp.ailearningassistantmicroservice.dto.request.CreatePerformanceAnalysisRequest;
import exp.ailearningassistantmicroservice.dto.request.UpdatePerformanceAnalysisRequest;
import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;

import java.util.List;

public interface PerformanceAnalysisService {

    PerformanceAnalysis create(CreatePerformanceAnalysisRequest request);

    PerformanceAnalysis update(Long id, Long userId, UpdatePerformanceAnalysisRequest request);

    PerformanceAnalysis applyImpact(ApplyPerformanceImpactRequest request);

    PerformanceAnalysis getById(Long id);

    List<PerformanceAnalysis> getAll();

    List<PerformanceAnalysis> getByUserId(Long userId);

    List<PerformanceAnalysis> getByCourseId(Long courseId);

    PerformanceAnalysis getByUserIdAndCourseId(Long userId, Long courseId);

    void delete(Long id, Long userId);
}
