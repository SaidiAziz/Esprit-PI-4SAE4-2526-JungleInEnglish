package com.example.demo;

import com.example.demo.QuizMS.Level;
import com.example.demo.QuizMS.Quiz;
import com.example.demo.QuizMS.QuizRepository;
import com.example.demo.QuizMS.QuizService;
import com.example.demo.QuizMS.QuizStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuizService quizService;

    private Quiz quiz1;
    private Quiz quiz2;

    @BeforeEach
    void setUp() {
        quiz1 = new Quiz("Java Basics", Level.A1, 30, 60, false);
        quiz1.setStatus(QuizStatus.PUBLISHED);
        quiz1.setMaxAttempts(3);
        quiz1.setCreatedBy(1L);

        quiz2 = new Quiz("Spring Advanced", Level.B2, 60, 70, true);
        quiz2.setStatus(QuizStatus.DRAFT);
        quiz2.setMaxAttempts(2);
        quiz2.setCreatedBy(2L);
    }

    // ──────────────────────────────────────────────
    //  findAll
    // ──────────────────────────────────────────────

    @Test
    void findAll_shouldReturnAllQuizzes() {
        when(quizRepository.findAll()).thenReturn(List.of(quiz1, quiz2));

        List<Quiz> result = quizService.findAll();

        assertThat(result).hasSize(2).containsExactly(quiz1, quiz2);
        verify(quizRepository, times(1)).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoQuizzes() {
        when(quizRepository.findAll()).thenReturn(List.of());

        assertThat(quizService.findAll()).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  findById
    // ──────────────────────────────────────────────

    @Test
    void findById_shouldReturnQuizWhenFound() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));

        Optional<Quiz> result = quizService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Java Basics");
        assertThat(result.get().getLevel()).isEqualTo(Level.A1);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(quizService.findById(99L)).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  findByLevel
    // ──────────────────────────────────────────────

    @Test
    void findByLevel_shouldReturnMatchingQuizzes() {
        when(quizRepository.findByLevel(Level.A1)).thenReturn(List.of(quiz1));

        List<Quiz> result = quizService.findByLevel(Level.A1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevel()).isEqualTo(Level.A1);
        verify(quizRepository).findByLevel(Level.A1);
    }

    @Test
    void findByLevel_shouldReturnEmptyListWhenNoMatch() {
        when(quizRepository.findByLevel(Level.A2)).thenReturn(List.of());

        assertThat(quizService.findByLevel(Level.A2)).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  save
    // ──────────────────────────────────────────────

    @Test
    void save_shouldPersistAndReturnQuiz() {
        when(quizRepository.save(quiz1)).thenReturn(quiz1);

        Quiz saved = quizService.save(quiz1);

        assertThat(saved).isEqualTo(quiz1);
        assertThat(saved.getTitle()).isEqualTo("Java Basics");
        verify(quizRepository, times(1)).save(quiz1);
    }

    @Test
    void save_shouldPreserveAllFields() {
        when(quizRepository.save(quiz2)).thenReturn(quiz2);

        Quiz saved = quizService.save(quiz2);

        assertThat(saved.getLevel()).isEqualTo(Level.B2);
        assertThat(saved.getDuration()).isEqualTo(60);
        assertThat(saved.getPassingScore()).isEqualTo(70);
        assertThat(saved.getIsAdaptive()).isTrue();
    }

    // ──────────────────────────────────────────────
    //  update
    // ──────────────────────────────────────────────

    @Test
    void update_shouldModifyAllProvidedFields() {
        // incoming uses B1 → after update, level must be B1 (not the original A1)
        Quiz incoming = new Quiz("Updated Title", Level.B1, 45, 75, true);
        incoming.setStatus(QuizStatus.PUBLISHED);
        incoming.setMaxAttempts(5);

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));

        Quiz updated = quizService.update(1L, incoming);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getLevel()).isEqualTo(Level.B1);   // ← fixed: B1 not B2
        assertThat(updated.getDuration()).isEqualTo(45);
        assertThat(updated.getPassingScore()).isEqualTo(75);
        assertThat(updated.getIsAdaptive()).isTrue();
        assertThat(updated.getMaxAttempts()).isEqualTo(5);
        assertThat(updated.getStatus()).isEqualTo(QuizStatus.PUBLISHED);
        verify(quizRepository).save(quiz1);
    }

    @Test
    void update_shouldNotChangeStatusWhenIncomingStatusIsNull() {
        Quiz incoming = new Quiz("New Title", Level.B2, 50, 80, false);
        incoming.setStatus(null);

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));

        Quiz updated = quizService.update(1L, incoming);

        assertThat(updated.getStatus()).isEqualTo(QuizStatus.PUBLISHED);
    }

    @Test
    void update_shouldChangeStatusWhenProvided() {
        Quiz incoming = new Quiz("Title", Level.A1, 30, 60, false);
        incoming.setStatus(QuizStatus.ARCHIVED);

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));

        Quiz updated = quizService.update(1L, incoming);

        assertThat(updated.getStatus()).isEqualTo(QuizStatus.ARCHIVED);
    }

    @Test
    void update_shouldThrowIllegalArgumentExceptionWhenNotFound() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.update(99L, quiz1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quiz not found")
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldNotCallSaveWhenQuizNotFound() {
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.update(99L, quiz1))
                .isInstanceOf(IllegalArgumentException.class);

        verify(quizRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────
    //  findByStatus
    // ──────────────────────────────────────────────

    @Test
    void findByStatus_shouldReturnPublishedQuizzes() {
        when(quizRepository.findByStatus(QuizStatus.PUBLISHED)).thenReturn(List.of(quiz1));

        List<Quiz> result = quizService.findByStatus(QuizStatus.PUBLISHED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(QuizStatus.PUBLISHED);
        verify(quizRepository).findByStatus(QuizStatus.PUBLISHED);
    }

    @Test
    void findByStatus_shouldReturnDraftQuizzes() {
        when(quizRepository.findByStatus(QuizStatus.DRAFT)).thenReturn(List.of(quiz2));

        List<Quiz> result = quizService.findByStatus(QuizStatus.DRAFT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(QuizStatus.DRAFT);
    }

    @Test
    void findByStatus_shouldReturnEmptyListWhenNoMatch() {
        when(quizRepository.findByStatus(QuizStatus.ARCHIVED)).thenReturn(List.of());

        assertThat(quizService.findByStatus(QuizStatus.ARCHIVED)).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  findByCreatedBy
    // ──────────────────────────────────────────────

    @Test
    void findByCreatedBy_shouldReturnQuizzesForTeacher() {
        when(quizRepository.findByCreatedBy(1L)).thenReturn(List.of(quiz1));

        List<Quiz> result = quizService.findByCreatedBy(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCreatedBy()).isEqualTo(1L);
        verify(quizRepository).findByCreatedBy(1L);
    }

    @Test
    void findByCreatedBy_shouldReturnEmptyListForUnknownTeacher() {
        when(quizRepository.findByCreatedBy(999L)).thenReturn(List.of());

        assertThat(quizService.findByCreatedBy(999L)).isEmpty();
    }

    // ──────────────────────────────────────────────
    //  deleteById
    // ──────────────────────────────────────────────

    @Test
    void deleteById_shouldCallRepositoryDelete() {
        doNothing().when(quizRepository).deleteById(1L);

        quizService.deleteById(1L);

        verify(quizRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_shouldNotThrowForExistingId() {
        doNothing().when(quizRepository).deleteById(1L);

        assertThatCode(() -> quizService.deleteById(1L)).doesNotThrowAnyException();
    }
}