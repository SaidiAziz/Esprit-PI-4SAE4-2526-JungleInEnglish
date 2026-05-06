package exp.ailearningassistantmicroservice.controller;

import exp.ailearningassistantmicroservice.dto.request.ApplyPerformanceImpactRequest;
import exp.ailearningassistantmicroservice.dto.request.CreatePerformanceAnalysisRequest;
import exp.ailearningassistantmicroservice.dto.request.UpdatePerformanceAnalysisRequest;
import exp.ailearningassistantmicroservice.dto.response.PerformanceAnalysisResponse;
import exp.ailearningassistantmicroservice.exception.BadRequestException;
import exp.ailearningassistantmicroservice.mapper.AiMapper;
import exp.ailearningassistantmicroservice.security.SecurityUtils;
import exp.ailearningassistantmicroservice.service.PerformanceAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/performance-analyses")
@RequiredArgsConstructor
public class PerformanceAnalysisController {

    private final PerformanceAnalysisService performanceAnalysisService;
    private final AiMapper aiMapper;

    @PostMapping
    public ResponseEntity<PerformanceAnalysisResponse> create(@Valid @RequestBody CreatePerformanceAnalysisRequest request) {
        enforceSelfAccess(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aiMapper.toResponse(performanceAnalysisService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerformanceAnalysisResponse> update(@PathVariable Long id,
                                                              @Valid @RequestBody UpdatePerformanceAnalysisRequest request) {
        return ResponseEntity.ok(aiMapper.toResponse(
                performanceAnalysisService.update(id, SecurityUtils.getCurrentUserId(), request)
        ));
    }

    @PostMapping("/impact")
    public ResponseEntity<PerformanceAnalysisResponse> applyImpact(@Valid @RequestBody ApplyPerformanceImpactRequest request) {
        enforceSelfAccess(request.getUserId());
        return ResponseEntity.ok(aiMapper.toResponse(performanceAnalysisService.applyImpact(request)));
    }

    @GetMapping
    public List<PerformanceAnalysisResponse> getAll() {
        return performanceAnalysisService.getAll().stream().map(aiMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public PerformanceAnalysisResponse getById(@PathVariable Long id) {
        return aiMapper.toResponse(performanceAnalysisService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public List<PerformanceAnalysisResponse> getByUser(@PathVariable Long userId) {
        return performanceAnalysisService.getByUserId(userId).stream().map(aiMapper::toResponse).toList();
    }

    @GetMapping("/course/{courseId}")
    public List<PerformanceAnalysisResponse> getByCourse(@PathVariable Long courseId) {
        return performanceAnalysisService.getByCourseId(courseId).stream().map(aiMapper::toResponse).toList();
    }

    @GetMapping("/me")
    public List<PerformanceAnalysisResponse> getMine() {
        Long userId = SecurityUtils.getCurrentUserId();
        return performanceAnalysisService.getByUserId(userId).stream().map(aiMapper::toResponse).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        performanceAnalysisService.delete(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    private void enforceSelfAccess(Long targetUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            throw new BadRequestException("You can only create analyses for your own user");
        }
    }
}
