package exp.collaborationroommicroservice.integration;

import exp.collaborationroommicroservice.integration.dto.ApplyPerformanceImpactRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiImpactService {

    private final AiServiceClient aiServiceClient;

    public void applyImpact(Long userId, Long courseId, int grammarDelta, int listeningDelta, int speakingDelta) {
        if (courseId == null) {
            return;
        }
        try {
            aiServiceClient.applyImpact(ApplyPerformanceImpactRequest.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .grammarDelta(grammarDelta)
                    .listeningDelta(listeningDelta)
                    .speakingDelta(speakingDelta)
                    .build());
        } catch (FeignException ignored) {
        }
    }
}
