package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.entities.StudentLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiRuleEngineTest {

    private final AiRuleEngine aiRuleEngine = new AiRuleEngine();

    @Test
    void calculatesAverageWithTwoDecimalRounding() {
        double average = aiRuleEngine.calculateAverage(70, 68, 60);

        assertEquals(66.0, average);
    }

    @Test
    void calculatesLevelUsingConfiguredThresholds() {
        assertEquals(StudentLevel.BEGINNER, aiRuleEngine.calculateLevel(49.99));
        assertEquals(StudentLevel.INTERMEDIATE, aiRuleEngine.calculateLevel(50.0));
        assertEquals(StudentLevel.INTERMEDIATE, aiRuleEngine.calculateLevel(75.0));
        assertEquals(StudentLevel.ADVANCED, aiRuleEngine.calculateLevel(75.01));
    }

    @Test
    void determinesWeakestSkillFromAnalysesAverage() {
        List<PerformanceAnalysis> analyses = List.of(
                PerformanceAnalysis.builder().grammarScore(80).listeningScore(61).speakingScore(72).build(),
                PerformanceAnalysis.builder().grammarScore(75).listeningScore(58).speakingScore(74).build()
        );

        SkillType weakestSkill = aiRuleEngine.determineWeakestSkill(analyses);

        assertEquals(SkillType.LISTENING, weakestSkill);
    }

    @Test
    void buildsLessonOrderMatchingWeakestSkillAndLevel() {
        String lessonOrder = aiRuleEngine.buildLessonOrder(SkillType.SPEAKING, StudentLevel.ADVANCED);

        assertTrue(lessonOrder.startsWith("Speaking Focus"));
        assertTrue(lessonOrder.endsWith("Fluency Mastery"));
    }
}
