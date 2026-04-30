package exp.ailearningassistantmicroservice.controller;

import exp.ailearningassistantmicroservice.dto.request.GenerateLearningPathRequest;
import exp.ailearningassistantmicroservice.dto.request.UpdateLearningPathProgressRequest;
import exp.ailearningassistantmicroservice.dto.response.LearningPathResponse;
import exp.ailearningassistantmicroservice.exception.BadRequestException;
import exp.ailearningassistantmicroservice.mapper.AiMapper;
import exp.ailearningassistantmicroservice.security.SecurityUtils;
import exp.ailearningassistantmicroservice.service.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ia/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;
    private final AiMapper aiMapper;

    @PostMapping("/generate")
    public LearningPathResponse generate(@Valid @RequestBody GenerateLearningPathRequest request) {
        enforceSelfAccess(request.getUserId());
        return aiMapper.toResponse(learningPathService.generateOrUpdate(request.getUserId(), request.getCourseId()));
    }

    @GetMapping("/{id}")
    public LearningPathResponse getById(@PathVariable Long id) {
        return aiMapper.toResponse(learningPathService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public List<LearningPathResponse> getByUser(@PathVariable Long userId) {
        return learningPathService.getByUserId(userId).stream().map(aiMapper::toResponse).toList();
    }

    @GetMapping("/me")
    public List<LearningPathResponse> getMine() {
        return learningPathService.getByUserId(SecurityUtils.getCurrentUserId()).stream().map(aiMapper::toResponse).toList();
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    public LearningPathResponse getByUserAndCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        return aiMapper.toResponse(learningPathService.getByUserIdAndCourseId(userId, courseId));
    }

    @PatchMapping("/{id}/progress")
    public LearningPathResponse updateProgress(@PathVariable Long id,
                                               @Valid @RequestBody UpdateLearningPathProgressRequest request) {
        return aiMapper.toResponse(learningPathService.updateProgress(id, SecurityUtils.getCurrentUserId(), request.getProgress()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        learningPathService.delete(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    private void enforceSelfAccess(Long targetUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            throw new BadRequestException("You can only generate learning paths for your own user");
        }
    }
}
