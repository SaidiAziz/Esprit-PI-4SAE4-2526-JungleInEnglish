import com.example.demo.QuestionMS.Question;
import com.example.demo.QuestionMS.QuestionRepository;
import com.example.demo.QuestionMS.QuestionService;
import com.example.demo.QuizMS.Quiz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionService questionService;

    private Quiz quiz;
    private Question question1;
    private Question question2;

    @BeforeEach
    void setUp() {
        quiz = new Quiz();

        question1 = new Question(quiz, "Quelle est la capitale de la France ?", 0, 5, List.of());
        question1.setId(1L);

        question2 = new Question(quiz, "Combien font 2 + 2 ?", 1, 10, List.of());
        question2.setId(2L);
    }

    // ──────────────────────────────────────────────
    //  findAll
    // ──────────────────────────────────────────────

    @Test
    void findAll_shouldReturnAllQuestions() {
        when(questionRepository.findAll()).thenReturn(Arrays.asList(question1, question2));

        List<Question> result = questionService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(question1, question2);
        verify(questionRepository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoQuestions() {
        when(questionRepository.findAll()).thenReturn(List.of());

        List<Question> result = questionService.findAll();

        assertThat(result).isEmpty();
        verify(questionRepository).findAll();
    }

    // ──────────────────────────────────────────────
    //  findById
    // ──────────────────────────────────────────────

    @Test
    void findById_shouldReturnQuestionWhenFound() {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question1));

        Optional<Question> result = questionService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getText()).isEqualTo("Quelle est la capitale de la France ?");
        assertThat(result.get().getPoints()).isEqualTo(5);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Question> result = questionService.findById(99L);

        assertThat(result).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  findByQuizId
    // ──────────────────────────────────────────────

    @Test
    void findByQuizId_shouldReturnQuestionsOrderedByIndex() {
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(1L))
                .thenReturn(Arrays.asList(question1, question2));

        List<Question> result = questionService.findByQuizId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderIndex())
                .isLessThanOrEqualTo(result.get(1).getOrderIndex());
        verify(questionRepository).findByQuizIdOrderByOrderIndexAsc(1L);
    }

    @Test
    void findByQuizId_shouldReturnEmptyListForUnknownQuiz() {
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(999L))
                .thenReturn(List.of());

        List<Question> result = questionService.findByQuizId(999L);

        assertThat(result).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  save
    // ──────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnQuestion() {
        when(questionRepository.save(question1)).thenReturn(question1);

        Question saved = questionService.save(question1);

        assertThat(saved).isEqualTo(question1);
        assertThat(saved.getText()).isEqualTo("Quelle est la capitale de la France ?");
        verify(questionRepository, times(1)).save(question1);
    }

    @Test
    void save_shouldPreservePointsAndOrderIndex() {
        Question q = new Question(quiz, "Question test ?", 3, 20, List.of());
        when(questionRepository.save(q)).thenReturn(q);

        Question saved = questionService.save(q);

        assertThat(saved.getPoints()).isEqualTo(20);
        assertThat(saved.getOrderIndex()).isEqualTo(3);
    }

    // ──────────────────────────────────────────────
    //  update
    // ──────────────────────────────────────────────

    @Test
    void update_shouldModifyAllFields() {
        Quiz newQuiz = new Quiz();
        Question incoming = new Question(newQuiz, "Nouveau texte ?", 5, 15, List.of());

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question1));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        Question updated = questionService.update(1L, incoming);

        assertThat(updated.getText()).isEqualTo("Nouveau texte ?");
        assertThat(updated.getOrderIndex()).isEqualTo(5);
        assertThat(updated.getPoints()).isEqualTo(15);
        assertThat(updated.getQuiz()).isEqualTo(newQuiz);
        verify(questionRepository).save(question1);
    }

    @Test
    void update_shouldUpdateQuizReference() {
        Quiz anotherQuiz = new Quiz();
        Question incoming = new Question(anotherQuiz, "Texte modifié ?", 0, 10, List.of());

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question1));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        Question updated = questionService.update(1L, incoming);

        assertThat(updated.getQuiz()).isEqualTo(anotherQuiz);
    }

    @Test
    void update_shouldThrowIllegalArgumentExceptionWhenNotFound() {
        Question incoming = new Question(quiz, "Texte ?", 0, 5, List.of());
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.update(99L, incoming))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Question non trouvée")
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldNotCallSaveWhenQuestionNotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.update(99L, question1))
                .isInstanceOf(IllegalArgumentException.class);

        verify(questionRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────
    //  deleteById
    // ──────────────────────────────────────────────

    @Test
    void deleteById_shouldCallRepositoryDelete() {
        doNothing().when(questionRepository).deleteById(1L);

        questionService.deleteById(1L);

        verify(questionRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_shouldNotThrowForExistingId() {
        doNothing().when(questionRepository).deleteById(1L);

        assertThatCode(() -> questionService.deleteById(1L)).doesNotThrowAnyException();
    }
}
 