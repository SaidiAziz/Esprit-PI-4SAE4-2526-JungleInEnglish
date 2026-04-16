package exp.ailearningassistantmicroservice.controller;

import exp.ailearningassistantmicroservice.dto.request.GenerateRecommendationsRequest;
import exp.ailearningassistantmicroservice.dto.response.RecommendationResponse;
import exp.ailearningassistantmicroservice.exception.BadRequestException;
import exp.ailearningassistantmicroservice.security.SecurityUtils;
import exp.ailearningassistantmicroservice.service.RecommendationResponseAssembler;
import exp.ailearningassistantmicroservice.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendationResponseAssembler recommendationResponseAssembler;

    @PostMapping("/generate")
    public List<RecommendationResponse> generate(@Valid @RequestBody GenerateRecommendationsRequest request) {
        enforceSelfAccess(request.getUserId());
        return recommendationResponseAssembler.toResponses(recommendationService.generateForUser(request.getUserId()));
    }

    @PostMapping("/generate/me")
    public List<RecommendationResponse> generateMine() {
        return recommendationResponseAssembler.toResponses(recommendationService.generateForUser(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/user/{userId}")
    public List<RecommendationResponse> getByUser(@PathVariable Long userId) {
        return recommendationResponseAssembler.toResponses(recommendationService.getByUserId(userId));
    }

    @GetMapping("/me")
    public List<RecommendationResponse> getMine() {
        return recommendationResponseAssembler.toResponses(recommendationService.getByUserId(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public RecommendationResponse getById(@PathVariable Long id) {
        return recommendationResponseAssembler.toResponse(recommendationService.getById(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        recommendationService.delete(id);
    }

    private void enforceSelfAccess(Long targetUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            throw new BadRequestException("You can only generate recommendations for your own user");
        }
    }
}
