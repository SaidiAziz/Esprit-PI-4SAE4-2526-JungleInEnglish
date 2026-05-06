package exp.ailearningassistantmicroservice.integration;

import exp.ailearningassistantmicroservice.exception.BadRequestException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileValidationService {

    private final UserProfileClient userProfileClient;

    public void validateCurrentStudentProfile() {
        try {
            userProfileClient.getCurrentStudentProfile();
        } catch (FeignException.NotFound ex) {
            throw new BadRequestException("No student profile found for the authenticated user");
        } catch (FeignException.Unauthorized ex) {
            throw new BadRequestException("Unable to validate student profile without a valid token");
        }
    }
}
