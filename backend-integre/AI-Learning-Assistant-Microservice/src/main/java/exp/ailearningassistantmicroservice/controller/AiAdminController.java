package exp.ailearningassistantmicroservice.controller;

import exp.ailearningassistantmicroservice.dto.response.AiAdminDashboardResponse;
import exp.ailearningassistantmicroservice.dto.response.AiAdminStudentOverviewResponse;
import exp.ailearningassistantmicroservice.dto.response.LearningPathResponse;
import exp.ailearningassistantmicroservice.dto.response.RecommendationResponse;
import exp.ailearningassistantmicroservice.dto.response.UserAiSummaryResponse;
import exp.ailearningassistantmicroservice.service.AiAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ia/admin")
@RequiredArgsConstructor
public class AiAdminController {

    private final AiAdminService aiAdminService;

    @GetMapping("/dashboard")
    public AiAdminDashboardResponse getDashboard() {
        return aiAdminService.getDashboard();
    }

    @GetMapping("/students")
    public List<AiAdminStudentOverviewResponse> getStudents() {
        return aiAdminService.getStudents();
    }

    @GetMapping("/students/{userId}")
    public UserAiSummaryResponse getStudentDetail(@PathVariable Long userId) {
        return aiAdminService.getStudentDetail(userId);
    }

    @GetMapping("/recommendations")
    public List<RecommendationResponse> getRecommendations() {
        return aiAdminService.getRecommendations();
    }

    @PostMapping("/recommendations/{userId}/regenerate")
    public List<RecommendationResponse> regenerateRecommendations(@PathVariable Long userId) {
        return aiAdminService.regenerateRecommendations(userId);
    }

    @GetMapping("/learning-paths")
    public List<LearningPathResponse> getLearningPaths() {
        return aiAdminService.getLearningPaths();
    }

    @PostMapping("/learning-paths/{userId}/regenerate/{courseId}")
    public LearningPathResponse regenerateLearningPath(@PathVariable Long userId, @PathVariable Long courseId) {
        return aiAdminService.regenerateLearningPath(userId, courseId);
    }
}
