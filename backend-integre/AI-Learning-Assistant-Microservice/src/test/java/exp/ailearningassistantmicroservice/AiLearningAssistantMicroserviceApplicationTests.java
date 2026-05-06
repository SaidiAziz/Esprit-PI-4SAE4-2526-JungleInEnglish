package exp.ailearningassistantmicroservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AiLearningAssistantMicroserviceApplicationTests {

    @Test
    void applicationClassIsLoadable() {
        assertDoesNotThrow(() -> AiLearningAssistantMicroserviceApplication.class.getDeclaredConstructor());
    }
}
