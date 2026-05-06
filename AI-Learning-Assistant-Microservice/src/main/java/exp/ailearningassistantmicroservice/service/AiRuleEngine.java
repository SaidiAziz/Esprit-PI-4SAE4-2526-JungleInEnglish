package exp.ailearningassistantmicroservice.service;

import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.entities.SkillType;
import exp.ailearningassistantmicroservice.entities.StudentLevel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiRuleEngine {

    public double calculateAverage(int grammarScore, int listeningScore, int speakingScore) {
        return Math.round(((grammarScore + listeningScore + speakingScore) / 3.0) * 100.0) / 100.0;
    }

    public StudentLevel calculateLevel(double averageScore) {
        if (averageScore < 50) {
            return StudentLevel.BEGINNER;
        }
        if (averageScore <= 75) {
            return StudentLevel.INTERMEDIATE;
        }
        return StudentLevel.ADVANCED;
    }

    public SkillType determineWeakestSkill(int grammarScore, int listeningScore, int speakingScore) {
        if (grammarScore <= listeningScore && grammarScore <= speakingScore) {
            return SkillType.GRAMMAR;
        }
        if (listeningScore <= grammarScore && listeningScore <= speakingScore) {
            return SkillType.LISTENING;
        }
        return SkillType.SPEAKING;
    }

    public SkillType determineWeakestSkill(List<PerformanceAnalysis> analyses) {
        if (analyses.isEmpty()) {
            return SkillType.GRAMMAR;
        }

        double grammarAverage = analyses.stream().mapToInt(PerformanceAnalysis::getGrammarScore).average().orElse(0);
        double listeningAverage = analyses.stream().mapToInt(PerformanceAnalysis::getListeningScore).average().orElse(0);
        double speakingAverage = analyses.stream().mapToInt(PerformanceAnalysis::getSpeakingScore).average().orElse(0);

        return determineWeakestSkill((int) Math.round(grammarAverage), (int) Math.round(listeningAverage), (int) Math.round(speakingAverage));
    }

    public String buildLessonOrder(SkillType weakestSkill, StudentLevel level) {
        String levelStep = switch (level) {
            case BEGINNER -> "Core Foundations";
            case INTERMEDIATE -> "Applied Practice";
            case ADVANCED -> "Fluency Mastery";
        };

        return switch (weakestSkill) {
            case GRAMMAR -> "Grammar Focus -> Vocabulary Builder -> Listening Workshop -> Speaking Lab -> " + levelStep;
            case LISTENING -> "Listening Focus -> Pronunciation Drill -> Grammar Refresh -> Speaking Lab -> " + levelStep;
            case SPEAKING -> "Speaking Focus -> Pronunciation Drill -> Listening Workshop -> Grammar Refresh -> " + levelStep;
        };
    }
}
