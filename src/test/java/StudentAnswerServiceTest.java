import com.example.demo.QuizAttempMS.QuizAttempt;
import com.example.demo.QuizAttempMS.QuizAttemptRepository;
import com.example.demo.StudentAnswerMS.StudentAnswer;
import com.example.demo.StudentAnswerMS.StudentAnswerRepository;
import com.example.demo.StudentAnswerMS.StudentAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentAnswerServiceTest {

    @Mock
    private StudentAnswerRepository studentAnswerRepository;

    @Mock
    private QuizAttemptRepository attemptRepository;

    @InjectMocks
    private StudentAnswerService studentAnswerService;

    private QuizAttempt attempt;
    private StudentAnswer answer1;
    private StudentAnswer answer2;

    @BeforeEach
    void setUp() {
        attempt = new QuizAttempt();
        attempt.setId(1L);
        attempt.setStudentId(10L);
        attempt.setQuizId(5L);

        answer1 = new StudentAnswer();
        answer1.setId(1L);
        answer1.setAttempt(attempt);
        answer1.setQuestionId(100L);
        answer1.setSelectedAnswerId(200L);
        answer1.setIsCorrect(true);
        answer1.setPointsEarned(10);
        answer1.setResponseTimeSeconds(4);

        answer2 = new StudentAnswer();
        answer2.setId(2L);
        answer2.setAttempt(attempt);
        answer2.setQuestionId(101L);
        answer2.setSelectedAnswerId(201L);
        answer2.setIsCorrect(false);
        answer2.setPointsEarned(0);
        answer2.setResponseTimeSeconds(12);
    }

    // ──────────────────────────────────────────────
    //  getByAttempt
    // ──────────────────────────────────────────────

    @Test
    void getByAttempt_shouldReturnAnswersForExistingAttempt() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(studentAnswerRepository.findByAttempt(attempt)).thenReturn(List.of(answer1, answer2));

        List<StudentAnswer> result = studentAnswerService.getByAttempt(1L);

        assertThat(result).hasSize(2).containsExactly(answer1, answer2);
        verify(attemptRepository).findById(1L);
        verify(studentAnswerRepository).findByAttempt(attempt);
    }

    @Test
    void getByAttempt_shouldReturnEmptyListWhenNoAnswers() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(studentAnswerRepository.findByAttempt(attempt)).thenReturn(List.of());

        List<StudentAnswer> result = studentAnswerService.getByAttempt(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getByAttempt_shouldThrowWhenAttemptNotFound() {
        when(attemptRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentAnswerService.getByAttempt(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Attempt not found")
                .hasMessageContaining("99");

        verify(studentAnswerRepository, never()).findByAttempt(any());
    }

    // ──────────────────────────────────────────────
    //  getById
    // ──────────────────────────────────────────────

    @Test
    void getById_shouldReturnAnswerWhenFound() {
        when(studentAnswerRepository.findById(1L)).thenReturn(Optional.of(answer1));

        StudentAnswer result = studentAnswerService.getById(1L);

        assertThat(result).isEqualTo(answer1);
        assertThat(result.getIsCorrect()).isTrue();
        assertThat(result.getPointsEarned()).isEqualTo(10);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(studentAnswerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentAnswerService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("StudentAnswer not found")
                .hasMessageContaining("99");
    }

    // ──────────────────────────────────────────────
    //  getByStudentAndQuestion
    // ──────────────────────────────────────────────

    @Test
    void getByStudentAndQuestion_shouldReturnMatchingAnswers() {
        when(studentAnswerRepository.findByStudentAndQuestion(10L, 100L))
                .thenReturn(List.of(answer1));

        List<StudentAnswer> result = studentAnswerService.getByStudentAndQuestion(10L, 100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuestionId()).isEqualTo(100L);
        verify(studentAnswerRepository).findByStudentAndQuestion(10L, 100L);
    }

    @Test
    void getByStudentAndQuestion_shouldReturnEmptyListWhenNoMatch() {
        when(studentAnswerRepository.findByStudentAndQuestion(99L, 999L))
                .thenReturn(List.of());

        assertThat(studentAnswerService.getByStudentAndQuestion(99L, 999L)).isEmpty();
    }

    @Test
    void getByStudentAndQuestion_shouldReturnMultipleAnswersForSameQuestion() {
        when(studentAnswerRepository.findByStudentAndQuestion(10L, 100L))
                .thenReturn(List.of(answer1, answer2));

        List<StudentAnswer> result = studentAnswerService.getByStudentAndQuestion(10L, 100L);

        assertThat(result).hasSize(2);
    }
}