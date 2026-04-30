package exp.ailearningassistantmicroservice.service.impl;

import exp.ailearningassistantmicroservice.dto.request.ApplyPerformanceImpactRequest;
import exp.ailearningassistantmicroservice.dto.request.CreatePerformanceAnalysisRequest;
import exp.ailearningassistantmicroservice.dto.request.UpdatePerformanceAnalysisRequest;
import exp.ailearningassistantmicroservice.entities.LearningPath;
import exp.ailearningassistantmicroservice.entities.PerformanceAnalysis;
import exp.ailearningassistantmicroservice.entities.StudentLevel;
import exp.ailearningassistantmicroservice.exception.BadRequestException;
import exp.ailearningassistantmicroservice.integration.UserProfileValidationService;
import exp.ailearningassistantmicroservice.repositories.LearningPathRepository;
import exp.ailearningassistantmicroservice.repositories.PerformanceAnalysisRepository;
import exp.ailearningassistantmicroservice.service.AiRuleEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceAnalysisServiceImplTest {

    @Mock
    private PerformanceAnalysisRepository performanceAnalysisRepository;

    @Mock
    private LearningPathRepository learningPathRepository;

    @Mock
    private UserProfileValidationService userProfileValidationService;

    @Spy
    private AiRuleEngine aiRuleEngine;

    @InjectMocks
    private PerformanceAnalysisServiceImpl performanceAnalysisService;

    @Test
    void createComputesAverageAndLevelBeforeSaving() {
        CreatePerformanceAnalysisRequest request = new CreatePerformanceAnalysisRequest();
        request.setUserId(3L);
        request.setCourseId(101L);
        request.setGrammarScore(70);
        request.setListeningScore(68);
        request.setSpeakingScore(60);

        when(performanceAnalysisRepository.findByUserIdAndCourseId(3L, 101L)).thenReturn(Optional.empty());
        when(performanceAnalysisRepository.save(any(PerformanceAnalysis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerformanceAnalysis result = performanceAnalysisService.create(request);

        assertEquals(66.0, result.getAverageScore());
        assertEquals(StudentLevel.INTERMEDIATE, result.getLevel());
        verify(userProfileValidationService).validateCurrentStudentProfile();
    }

    @Test
    void createRejectsDuplicateAnalysisForSameUserAndCourse() {
        CreatePerformanceAnalysisRequest request = new CreatePerformanceAnalysisRequest();
        request.setUserId(3L);
        request.setCourseId(101L);

        when(performanceAnalysisRepository.findByUserIdAndCourseId(3L, 101L))
                .thenReturn(Optional.of(PerformanceAnalysis.builder().id(99L).userId(3L).courseId(101L).build()));

        assertThrows(BadRequestException.class, () -> performanceAnalysisService.create(request));
        verify(performanceAnalysisRepository, never()).save(any());
    }

    @Test
    void applyImpactClampsScoresAndSynchronizesLearningPathProgress() {
        ApplyPerformanceImpactRequest request = new ApplyPerformanceImpactRequest();
        request.setUserId(7L);
        request.setCourseId(202L);
        request.setGrammarDelta(20);
        request.setListeningDelta(-100);
        request.setSpeakingDelta(10);

        PerformanceAnalysis existing = PerformanceAnalysis.builder()
                .id(5L)
                .userId(7L)
                .courseId(202L)
                .grammarScore(90)
                .listeningScore(15)
                .speakingScore(85)
                .build();

        LearningPath path = LearningPath.builder()
                .id(8L)
                .userId(7L)
                .courseId(202L)
                .progress(0)
                .build();

        when(performanceAnalysisRepository.findByUserIdAndCourseId(7L, 202L)).thenReturn(Optional.of(existing));
        when(performanceAnalysisRepository.save(any(PerformanceAnalysis.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(learningPathRepository.findByUserIdAndCourseId(7L, 202L)).thenReturn(Optional.of(path));
        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerformanceAnalysis result = performanceAnalysisService.applyImpact(request);

        assertEquals(100, result.getGrammarScore());
        assertEquals(0, result.getListeningScore());
        assertEquals(95, result.getSpeakingScore());
        assertEquals(65.0, result.getAverageScore());
        assertEquals(65.0, path.getProgress());
    }

    @Test
    void updateRejectsModificationOfAnotherUsersAnalysis() {
        PerformanceAnalysis existing = PerformanceAnalysis.builder()
                .id(11L)
                .userId(4L)
                .courseId(301L)
                .grammarScore(60)
                .listeningScore(60)
                .speakingScore(60)
                .averageScore(60)
                .level(StudentLevel.INTERMEDIATE)
                .createdAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();

        UpdatePerformanceAnalysisRequest request = new UpdatePerformanceAnalysisRequest();
        request.setCourseId(301L);
        request.setGrammarScore(65);
        request.setListeningScore(65);
        request.setSpeakingScore(65);

        when(performanceAnalysisRepository.findById(11L)).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> performanceAnalysisService.update(11L, 99L, request));
    }
}
