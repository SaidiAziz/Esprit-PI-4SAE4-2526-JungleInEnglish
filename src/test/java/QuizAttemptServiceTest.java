import com.example.demo.AnswerMS.Answer;
import com.example.demo.AnswerMS.AttemptStatus;
import com.example.demo.QuestionMS.Question;
import com.example.demo.QuizAttempMS.QuizAttempt;
import com.example.demo.QuizAttempMS.QuizAttemptRepository;
import com.example.demo.QuizAttempMS.QuizAttemptService;
import com.example.demo.QuizMS.Quiz;
import com.example.demo.QuizMS.QuizRepository;
import com.example.demo.QuizMS.QuizStatus;
import com.example.demo.StudentAnswerMS.StudentAnswer;
import com.example.demo.StudentAnswerMS.StudentAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceTest {

    @Mock private QuizAttemptRepository attemptRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private StudentAnswerRepository studentAnswerRepository;

    @InjectMocks
    private QuizAttemptService quizAttemptService;

    private Quiz quiz;
    private Question question;
    private Answer correctAnswer;
    private Answer wrongAnswer;
    private QuizAttempt attempt;

    // ── Helper: inject private id field via reflection ─────────────
    private static void setId(Object target, long value) {
        try {
            Field f = target.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Could not set id on " + target.getClass().getSimpleName(), e);
        }
    }

    @BeforeEach
    void setUp() {
        // FIX 1: Answer.getId() was null because Answer has no setId().
        //        Use reflection to assign the private id field directly.
        correctAnswer = new Answer();
        setId(correctAnswer, 10L);
        correctAnswer.setIsCorrect(true);

        wrongAnswer = new Answer();
        setId(wrongAnswer, 20L);
        wrongAnswer.setIsCorrect(false);

        question = new Question();
        question.setId(1L);
        question.setPoints(10);
        question.setAnswers(new ArrayList<>(List.of(correctAnswer, wrongAnswer)));

        // FIX 2: quiz.getPassingScore() was null → NPE in calculateFinalScore().
        //        Always set passingScore in the shared quiz fixture.
        quiz = new Quiz();
        quiz.setStatus(QuizStatus.PUBLISHED);
        quiz.setMaxAttempts(3);
        quiz.setPassingScore(60);   // ← was missing
        quiz.setDuration(10);
        quiz.setQuestions(List.of(question));

        attempt = new QuizAttempt();
        attempt.setId(1L);
        attempt.setQuizId(1L);
        attempt.setStudentId(1L);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setLivesRemaining(3);
        attempt.setScore(0);
        attempt.setTotalPoints(10);
        attempt.setPercentage(0.0);
        attempt.setAttemptNumber(1);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(5));
    }

    // ──────────────────────────────────────────────
    //  startQuiz
    // ──────────────────────────────────────────────

    @Test
    void startQuiz_shouldCreateNewAttemptWhenNoneExists() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findByStudentIdAndQuizIdAndStatus(1L, 1L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(attemptRepository.countByStudentIdAndQuizId(1L, 1L)).thenReturn(0L);
        when(attemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.startQuiz(1L, 1L, null);

        assertThat(result.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
        assertThat(result.getLivesRemaining()).isEqualTo(3);
        assertThat(result.getAttemptNumber()).isEqualTo(1);
        assertThat(result.getTotalPoints()).isEqualTo(10);
        verify(attemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void startQuiz_shouldResumeExistingInProgressAttempt() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findByStudentIdAndQuizIdAndStatus(1L, 1L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.of(attempt));

        QuizAttempt result = quizAttemptService.startQuiz(1L, 1L, null);

        assertThat(result).isEqualTo(attempt);
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void startQuiz_shouldThrowWhenQuizNotFound() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizAttemptService.startQuiz(99L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quiz not found");
    }

    @Test
    void startQuiz_shouldThrowWhenQuizNotPublished() {
        quiz.setStatus(QuizStatus.DRAFT);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> quizAttemptService.startQuiz(1L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not published");
    }

    @Test
    void startQuiz_shouldThrowWhenMaxAttemptsReached() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findByStudentIdAndQuizIdAndStatus(1L, 1L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(attemptRepository.countByStudentIdAndQuizId(1L, 1L)).thenReturn(3L);

        assertThatThrownBy(() -> quizAttemptService.startQuiz(1L, 1L, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Maximum attempts reached");
    }

    @Test
    void startQuiz_shouldSetCorrectAttemptNumber() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findByStudentIdAndQuizIdAndStatus(1L, 1L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(attemptRepository.countByStudentIdAndQuizId(1L, 1L)).thenReturn(2L);
        when(attemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.startQuiz(1L, 1L, null);

        assertThat(result.getAttemptNumber()).isEqualTo(3);
    }

    // ──────────────────────────────────────────────
    //  submitAnswer
    // ──────────────────────────────────────────────

    @Test
    void submitAnswer_shouldAddPointsWhenCorrect() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(studentAnswerRepository.findByAttemptAndQuestionId(attempt, 1L))
                .thenReturn(Optional.empty());
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.save(any())).thenReturn(attempt);

        Map<String, Object> result = quizAttemptService.submitAnswer(1L, 1L, correctAnswer.getId(), 3);

        assertThat(result.get("isCorrect")).isEqualTo(true);
        assertThat(result.get("gameOver")).isEqualTo(false);
        assertThat((int) result.get("pointsEarned")).isEqualTo(10);
        assertThat(attempt.getScore()).isEqualTo(10);
    }

    @Test
    void submitAnswer_shouldDecrementLivesWhenWrong() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(studentAnswerRepository.findByAttemptAndQuestionId(attempt, 1L))
                .thenReturn(Optional.empty());
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.save(any())).thenReturn(attempt);

        Map<String, Object> result = quizAttemptService.submitAnswer(1L, 1L, wrongAnswer.getId(), 10);

        assertThat(result.get("isCorrect")).isEqualTo(false);
        assertThat(attempt.getLivesRemaining()).isEqualTo(2);
    }

    @Test
    void submitAnswer_shouldTriggerGameOverWhenLastLifeLost() {
        attempt.setLivesRemaining(1);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(studentAnswerRepository.findByAttemptAndQuestionId(attempt, 1L))
                .thenReturn(Optional.empty());
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.save(any())).thenReturn(attempt);

        Map<String, Object> result = quizAttemptService.submitAnswer(1L, 1L, wrongAnswer.getId(), 10);

        assertThat(result.get("gameOver")).isEqualTo(true);
        assertThat(result.get("livesRemaining")).isEqualTo(0);
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.GAME_OVER);
    }

    @Test
    void submitAnswer_shouldReturnExistingResultWhenAlreadyAnswered() {
        StudentAnswer existing = new StudentAnswer();
        existing.setIsCorrect(true);
        existing.setPointsEarned(10);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(studentAnswerRepository.findByAttemptAndQuestionId(attempt, 1L))
                .thenReturn(Optional.of(existing));

        Map<String, Object> result = quizAttemptService.submitAnswer(1L, 1L, correctAnswer.getId(), 5);

        assertThat(result.get("isCorrect")).isEqualTo(true);
        assertThat(result.get("gameOver")).isEqualTo(false);
        verify(studentAnswerRepository, never()).save(any());
    }

    @Test
    void submitAnswer_shouldReturnEarlyWhenAttemptIsGameOver() {
        attempt.setStatus(AttemptStatus.GAME_OVER);
        attempt.setLivesRemaining(0);
        attempt.setPercentage(50.0);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        Map<String, Object> result = quizAttemptService.submitAnswer(1L, 1L, 10L, 5);

        assertThat(result.get("gameOver")).isEqualTo(true);
        assertThat(result.get("isCorrect")).isEqualTo(false);
        verify(quizRepository, never()).findById(any());
    }

    @Test
    void submitAnswer_shouldReturnEarlyWhenAttemptIsCompleted() {
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setPercentage(80.0);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        Map<String, Object> result = quizAttemptService.submitAnswer(1L, 1L, 10L, 5);

        assertThat(result.get("gameOver")).isEqualTo(false);
        verify(quizRepository, never()).findById(any());
    }

    @Test
    void submitAnswer_shouldThrowWhenAttemptNotFound() {
        when(attemptRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizAttemptService.submitAnswer(99L, 1L, 10L, 5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Attempt not found");
    }

    // ──────────────────────────────────────────────
    //  completeQuiz
    // ──────────────────────────────────────────────

    @Test
    void completeQuiz_shouldSetStatusToCompleted() {
        attempt.setScore(8);
        attempt.setTotalPoints(10);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.completeQuiz(1L);

        assertThat(result.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getPercentage()).isEqualTo(80.0);
    }

    @Test
    void completeQuiz_shouldReturnAttemptAsIsWhenAlreadyGameOver() {
        attempt.setStatus(AttemptStatus.GAME_OVER);
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        QuizAttempt result = quizAttemptService.completeQuiz(1L);

        assertThat(result.getStatus()).isEqualTo(AttemptStatus.GAME_OVER);
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void completeQuiz_shouldReturnAttemptAsIsWhenAlreadyCompleted() {
        attempt.setStatus(AttemptStatus.COMPLETED);
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        QuizAttempt result = quizAttemptService.completeQuiz(1L);

        assertThat(result.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void completeQuiz_shouldMarkAsPassedWhenScoreAboveThreshold() {
        attempt.setScore(7);
        attempt.setTotalPoints(10);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.completeQuiz(1L);

        assertThat(result.getIsPassed()).isTrue(); // 70% >= 60%
    }

    @Test
    void completeQuiz_shouldMarkAsFailedWhenScoreBelowThreshold() {
        attempt.setScore(4);
        attempt.setTotalPoints(10);

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.completeQuiz(1L);

        assertThat(result.getIsPassed()).isFalse(); // 40% < 60%
    }

    // ──────────────────────────────────────────────
    //  timeoutQuiz
    // ──────────────────────────────────────────────

    @Test
    void timeoutQuiz_shouldSetStatusToTimedOut() {
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(10));

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.timeoutQuiz(1L);

        assertThat(result.getStatus()).isEqualTo(AttemptStatus.TIMED_OUT);
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getDurationSeconds()).isPositive();
    }

    @Test
    void timeoutQuiz_shouldThrowWhenAttemptNotFound() {
        when(attemptRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizAttemptService.timeoutQuiz(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Attempt not found");
    }

    // ──────────────────────────────────────────────
    //  Read methods
    // ──────────────────────────────────────────────

    @Test
    void getAttemptById_shouldReturnAttemptWhenFound() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        assertThat(quizAttemptService.getAttemptById(1L)).isEqualTo(attempt);
    }

    @Test
    void getAttemptById_shouldThrowWhenNotFound() {
        when(attemptRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizAttemptService.getAttemptById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Attempt not found");
    }

    @Test
    void getByStudent_shouldDelegateToRepository() {
        when(attemptRepository.findByStudentId(1L)).thenReturn(List.of(attempt));
        assertThat(quizAttemptService.getByStudent(1L)).containsExactly(attempt);
    }

    @Test
    void getByQuiz_shouldDelegateToRepository() {
        when(attemptRepository.findByQuizId(1L)).thenReturn(List.of(attempt));
        assertThat(quizAttemptService.getByQuiz(1L)).containsExactly(attempt);
    }

    @Test
    void getCompletedByStudent_shouldReturnOnlyCompletedAttempts() {
        attempt.setStatus(AttemptStatus.COMPLETED);
        when(attemptRepository.findByStudentIdAndStatus(1L, AttemptStatus.COMPLETED))
                .thenReturn(List.of(attempt));

        assertThat(quizAttemptService.getCompletedByStudent(1L))
                .allMatch(a -> a.getStatus() == AttemptStatus.COMPLETED);
    }

    // ──────────────────────────────────────────────
    //  getStudentStats
    // ──────────────────────────────────────────────

    @Test
    void getStudentStats_shouldReturnCorrectLevelAndXp() {
        when(attemptRepository.findTotalXp(1L)).thenReturn(250);
        when(attemptRepository.countCompleted(1L)).thenReturn(5L);
        when(attemptRepository.countPerfectScores(1L)).thenReturn(1L);
        when(attemptRepository.countActiveDaysLast30(1L)).thenReturn(10);

        Map<String, Object> stats = quizAttemptService.getStudentStats(1L);

        assertThat(stats.get("totalXp")).isEqualTo(250);
        assertThat(stats.get("level")).isEqualTo(Math.max(1, 250 / 100));
        assertThat(stats.get("xpToNextLevel")).isEqualTo(100 - (250 % 100));
        assertThat(stats.get("completed")).isEqualTo(5L);
        assertThat(stats).containsKey("badges");
    }

    @Test
    void getStudentStats_shouldReturnLevelOneWhenXpIsZero() {
        when(attemptRepository.findTotalXp(1L)).thenReturn(0);
        when(attemptRepository.countCompleted(1L)).thenReturn(0L);
        when(attemptRepository.countPerfectScores(1L)).thenReturn(0L);
        when(attemptRepository.countActiveDaysLast30(1L)).thenReturn(0);

        assertThat(quizAttemptService.getStudentStats(1L).get("level")).isEqualTo(1);
    }

    // ──────────────────────────────────────────────
    //  getDetailedQuizStats
    // ──────────────────────────────────────────────

    @Test
    void getDetailedQuizStats_shouldComputePassRateAndAverageScore() {
        when(attemptRepository.findByQuizId(1L))
                .thenReturn(List.of(buildCompleted(true, 80.0, 120, 10),
                        buildCompleted(false, 40.0, 200, 5)));

        Map<String, Object> stats = quizAttemptService.getDetailedQuizStats(1L);

        assertThat(stats.get("totalAttempts")).isEqualTo(2);
        assertThat(stats.get("passed")).isEqualTo(1L);
        assertThat(stats.get("failed")).isEqualTo(1L);
        assertThat(stats.get("passRate")).isEqualTo(50L);  // Math.round → long
        assertThat((double) stats.get("avgScore")).isEqualTo(60.0);
    }

    @Test
    void getDetailedQuizStats_shouldReturnZeroPassRateWhenNoCompleted() {
        when(attemptRepository.findByQuizId(1L)).thenReturn(List.of());

        Map<String, Object> stats = quizAttemptService.getDetailedQuizStats(1L);

        // FIX 3: empty branch uses plain `0` (int), non-empty uses Math.round (long)
        assertThat(stats.get("passRate")).isEqualTo(0L);
        assertThat(stats.get("totalAttempts")).isEqualTo(0);
    }

    // ──────────────────────────────────────────────
    //  getStudentProgression
    // ──────────────────────────────────────────────

    @Test
    void getStudentProgression_shouldReturnCumulativeXpInOrder() {
        QuizAttempt a1 = buildCompleted(true, 70.0, null, 30);
        a1.setQuizId(1L);
        a1.setCompletedAt(LocalDateTime.now().minusDays(2));

        QuizAttempt a2 = buildCompleted(true, 90.0, null, 50);
        a2.setQuizId(2L);
        a2.setCompletedAt(LocalDateTime.now().minusDays(1));

        when(attemptRepository.findByStudentId(1L)).thenReturn(List.of(a1, a2));

        List<Map<String, Object>> prog = quizAttemptService.getStudentProgression(1L);

        assertThat(prog).hasSize(2);
        assertThat(prog.get(0).get("cumulativeXp")).isEqualTo(30L);
        assertThat(prog.get(1).get("cumulativeXp")).isEqualTo(80L);
    }

    @Test
    void getStudentProgression_shouldIgnoreNonCompletedAttempts() {
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        when(attemptRepository.findByStudentId(1L)).thenReturn(List.of(attempt));

        assertThat(quizAttemptService.getStudentProgression(1L)).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  getStudentBadges
    // ──────────────────────────────────────────────

    @Test
    void getStudentBadges_shouldUnlockFirstQuizBadgeWhenOneCompleted() {
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setPercentage(70.0);
        attempt.setXpEarned(20);
        attempt.setIsPassed(true);
        attempt.setCompletedAt(LocalDateTime.now());
        when(attemptRepository.findByStudentId(1L)).thenReturn(List.of(attempt));

        boolean unlocked = quizAttemptService.getStudentBadges(1L).stream()
                .filter(b -> "first_quiz".equals(b.get("id")))
                .findFirst().orElseThrow()
                .get("unlocked").equals(true);

        assertThat(unlocked).isTrue();
    }

    @Test
    void getStudentBadges_shouldNotUnlockPerfectScoreBadgeWhenNoPerfectScore() {
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setPercentage(80.0);
        attempt.setXpEarned(10);
        attempt.setIsPassed(true);
        attempt.setCompletedAt(LocalDateTime.now());
        when(attemptRepository.findByStudentId(1L)).thenReturn(List.of(attempt));

        boolean unlocked = quizAttemptService.getStudentBadges(1L).stream()
                .filter(b -> "perfect_score".equals(b.get("id")))
                .findFirst().orElseThrow()
                .get("unlocked").equals(true);

        assertThat(unlocked).isFalse();
    }

    @Test
    void getStudentBadges_shouldReturnEmptyBadgesWhenNoAttempts() {
        when(attemptRepository.findByStudentId(1L)).thenReturn(List.of());

        boolean hasFirstQuiz = quizAttemptService.getStudentBadges(1L).stream()
                .anyMatch(b -> "first_quiz".equals(b.get("id")));

        assertThat(hasFirstQuiz).isFalse();
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────

    private QuizAttempt buildCompleted(boolean isPassed, double percentage,
                                       Integer durationSec, int xp) {
        QuizAttempt a = new QuizAttempt();
        a.setStatus(AttemptStatus.COMPLETED);
        a.setIsPassed(isPassed);
        a.setPercentage(percentage);
        a.setDurationSeconds(durationSec);
        a.setXpEarned(xp);
        a.setCompletedAt(LocalDateTime.now());
        return a;
    }
}