package com.example.demo;

import com.example.demo.AnswerMS.Answer;
import com.example.demo.AnswerMS.AnswerRepository;
import com.example.demo.AnswerMS.AnswerService;
import com.example.demo.QuestionMS.Question;
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
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private AnswerService answerService;

    private Question question;
    private Answer answer1;
    private Answer answer2;

    @BeforeEach
    void setUp() {
        question = new Question();
        // Use reflection or a setter if available; adjust to your Question class
        // question.setId(1L);

        answer1 = new Answer(question, "Paris", true, 0);
        answer2 = new Answer(question, "Londres", false, 1);
    }

    // ──────────────────────────────────────────────
    //  findAll
    // ──────────────────────────────────────────────

    @Test
    void findAll_shouldReturnAllAnswers() {
        when(answerRepository.findAll()).thenReturn(Arrays.asList(answer1, answer2));

        List<Answer> result = answerService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(answer1, answer2);
        verify(answerRepository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoAnswers() {
        when(answerRepository.findAll()).thenReturn(List.of());

        List<Answer> result = answerService.findAll();

        assertThat(result).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  findById
    // ──────────────────────────────────────────────

    @Test
    void findById_shouldReturnAnswerWhenFound() {
        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer1));

        Optional<Answer> result = answerService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getText()).isEqualTo("Paris");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(answerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Answer> result = answerService.findById(99L);

        assertThat(result).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  findByQuestionId
    // ──────────────────────────────────────────────

    @Test
    void findByQuestionId_shouldReturnAnswersOrderedByIndex() {
        when(answerRepository.findByQuestionIdOrderByOrderIndexAsc(1L))
                .thenReturn(Arrays.asList(answer1, answer2));

        List<Answer> result = answerService.findByQuestionId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderIndex()).isLessThanOrEqualTo(result.get(1).getOrderIndex());
        verify(answerRepository).findByQuestionIdOrderByOrderIndexAsc(1L);
    }

    @Test
    void findByQuestionId_shouldReturnEmptyListForUnknownQuestion() {
        when(answerRepository.findByQuestionIdOrderByOrderIndexAsc(999L))
                .thenReturn(List.of());

        List<Answer> result = answerService.findByQuestionId(999L);

        assertThat(result).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  save
    // ──────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnAnswer() {
        when(answerRepository.save(answer1)).thenReturn(answer1);

        Answer saved = answerService.save(answer1);

        assertThat(saved).isEqualTo(answer1);
        verify(answerRepository, times(1)).save(answer1);
    }

    @Test
    void save_shouldSetDefaultsWhenNullFields() {
        Answer minimal = new Answer(question, "Texte", null, null);
        when(answerRepository.save(any(Answer.class))).thenReturn(minimal);

        Answer saved = answerService.save(minimal);

        // Constructor defaults: isCorrect=false, orderIndex=0
        assertThat(saved.getIsCorrect()).isFalse();
        assertThat(saved.getOrderIndex()).isEqualTo(0);
    }

    // ──────────────────────────────────────────────
    //  update
    // ──────────────────────────────────────────────

    @Test
    void update_shouldModifyAllProvidedFields() {
        Answer incoming = new Answer(question, "Lyon", false, 2);

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer1));
        when(answerRepository.save(any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        Answer updated = answerService.update(1L, incoming);

        assertThat(updated.getText()).isEqualTo("Lyon");
        assertThat(updated.getIsCorrect()).isFalse();
        assertThat(updated.getOrderIndex()).isEqualTo(2);
        verify(answerRepository).save(answer1);
    }

    @Test
    void update_shouldNotChangeQuestionWhenIncomingQuestionIsNull() {
        Answer incoming = new Answer(null, "Lyon", false, 2);

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer1));
        when(answerRepository.save(any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        Answer updated = answerService.update(1L, incoming);

        // Question must remain unchanged
        assertThat(updated.getQuestion()).isEqualTo(question);
    }

    @Test
    void update_shouldChangeQuestionWhenProvided() {
        Question newQuestion = new Question();
        Answer incoming = new Answer(newQuestion, "Berlin", true, 3);

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer1));
        when(answerRepository.save(any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        Answer updated = answerService.update(1L, incoming);

        assertThat(updated.getQuestion()).isEqualTo(newQuestion);
    }

    @Test
    void update_shouldThrowWhenAnswerNotFound() {
        when(answerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.update(99L, answer1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Answer not found");
    }

    // ──────────────────────────────────────────────
    //  deleteById
    // ──────────────────────────────────────────────

    @Test
    void deleteById_shouldCallRepositoryDelete() {
        doNothing().when(answerRepository).deleteById(1L);

        answerService.deleteById(1L);

        verify(answerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_shouldNotThrowForExistingId() {
        doNothing().when(answerRepository).deleteById(1L);

        assertThatCode(() -> answerService.deleteById(1L)).doesNotThrowAnyException();
    }
}