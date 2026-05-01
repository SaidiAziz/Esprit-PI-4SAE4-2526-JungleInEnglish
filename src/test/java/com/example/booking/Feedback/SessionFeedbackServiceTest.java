package com.example.booking.Feedback;

import com.example.booking.SessionFeedbackMS.SessionFeedback;
import com.example.booking.SessionFeedbackMS.SessionFeedbackRepository;
import com.example.booking.SessionFeedbackMS.SessionFeedbackService;
import com.example.booking.SessionMS.Session;
import com.example.booking.SessionMS.SessionRepository;
import com.example.booking.SessionMS.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionFeedbackServiceTest {

    @Mock
    private SessionFeedbackRepository feedbackRepository;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionFeedbackService feedbackService;

    private Session session;
    private SessionFeedback feedback;

    // ── Helpers ──────────────────────────────────────────────────────
    private Session makeSession(Long id, SessionStatus status) {
        Session s = new Session();
        s.setId(id);
        s.setStatus(status);
        return s;
    }

    private SessionFeedback makeFeedback(Long id, Integer rating, String comment) {
        SessionFeedback f = new SessionFeedback();
        f.setId(id);
        f.setStudentId(100L);
        f.setRating(rating);
        f.setComment(comment);
        f.setCreatedAt(LocalDateTime.now());
        return f;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = makeSession(1L, SessionStatus.DONE);
        feedback = makeFeedback(1L, 4, "Très bonne session");
    }

    // ─────────────────────────────
    // ADD FEEDBACK ✅
    // ─────────────────────────────
    @Test
    void shouldAddFeedbackSuccessfully() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(feedbackRepository.existsBySessionId(1L)).thenReturn(false);
        when(feedbackRepository.save(any(SessionFeedback.class))).thenReturn(feedback);

        SessionFeedback result = feedbackService.addFeedback(1L, feedback);

        assertNotNull(result);
        assertEquals(4, result.getRating());
        verify(feedbackRepository, times(1)).save(any(SessionFeedback.class));
    }

    // ─────────────────────────────
    // ADD FEEDBACK — SESSION NOT DONE ❌
    // ─────────────────────────────
    @Test
    void shouldThrowWhenSessionNotDone() {
        session.setStatus(SessionStatus.ONGOING);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                feedbackService.addFeedback(1L, feedback)
        );

        assertTrue(ex.getMessage().contains("not completed yet"));
    }

    // ─────────────────────────────
    // ADD FEEDBACK — SESSION NOT FOUND ❌
    // ─────────────────────────────
    @Test
    void shouldThrowWhenSessionNotFound() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                feedbackService.addFeedback(99L, feedback)
        );

        assertTrue(ex.getMessage().contains("Session not found"));
    }

    // ─────────────────────────────
    // ADD FEEDBACK — ALREADY EXISTS ❌
    // ─────────────────────────────
    @Test
    void shouldThrowWhenFeedbackAlreadyExists() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(feedbackRepository.existsBySessionId(1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                feedbackService.addFeedback(1L, feedback)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    // ─────────────────────────────
    // ADD FEEDBACK — RATING INVALIDE ❌
    // ─────────────────────────────
    @Test
    void shouldThrowWhenRatingTooLow() {
        feedback.setRating(0); // < 1
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(feedbackRepository.existsBySessionId(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                feedbackService.addFeedback(1L, feedback)
        );

        assertTrue(ex.getMessage().contains("Rating must be between 1 and 5"));
    }

    @Test
    void shouldThrowWhenRatingTooHigh() {
        feedback.setRating(6); // > 5
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(feedbackRepository.existsBySessionId(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                feedbackService.addFeedback(1L, feedback)
        );

        assertTrue(ex.getMessage().contains("Rating must be between 1 and 5"));
    }

    // ─────────────────────────────
    // GET BY ID ✅
    // ─────────────────────────────
    @Test
    void shouldGetFeedbackById() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        SessionFeedback result = feedbackService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals(4, result.getRating());
    }

    // ─────────────────────────────
    // GET BY ID — NOT FOUND ❌
    // ─────────────────────────────
    @Test
    void shouldThrowWhenFeedbackNotFound() {
        when(feedbackRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                feedbackService.getById(99L)
        );

        assertTrue(ex.getMessage().contains("Feedback not found with id: 99"));
    }

    // ─────────────────────────────
    // GET BY SESSION ID ✅
    // ─────────────────────────────
    @Test
    void shouldGetFeedbackBySessionId() {
        when(feedbackRepository.findBySessionId(1L)).thenReturn(Optional.of(feedback));

        SessionFeedback result = feedbackService.getBySessionId(1L);

        assertNotNull(result);
    }

    // ─────────────────────────────
    // GET BY SESSION ID — NOT FOUND ❌
    // ─────────────────────────────
    @Test
    void shouldThrowWhenNoFeedbackForSession() {
        when(feedbackRepository.findBySessionId(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                feedbackService.getBySessionId(99L)
        );

        assertTrue(ex.getMessage().contains("No feedback for session"));
    }

    // ─────────────────────────────
    // GET ALL ✅
    // ─────────────────────────────
    @Test
    void shouldGetAllFeedbacks() {
        List<SessionFeedback> list = List.of(
                makeFeedback(1L, 4, "Bien"),
                makeFeedback(2L, 5, "Excellent")
        );
        when(feedbackRepository.findAll()).thenReturn(list);

        List<SessionFeedback> result = feedbackService.getAll();

        assertEquals(2, result.size());
    }

    // ─────────────────────────────
    // GET BY STUDENT ✅
    // ─────────────────────────────
    @Test
    void shouldGetFeedbackByStudent() {
        when(feedbackRepository.findByStudentId(100L)).thenReturn(List.of(feedback));

        List<SessionFeedback> result = feedbackService.getByStudent(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getStudentId());
    }

    // ─────────────────────────────
    // GET BY MIN RATING ✅
    // ─────────────────────────────
    @Test
    void shouldGetFeedbackByMinRating() {
        when(feedbackRepository.findByRatingGreaterThanEqual(4)).thenReturn(List.of(feedback));

        List<SessionFeedback> result = feedbackService.getByMinRating(4);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getRating() >= 4);
    }

    // ─────────────────────────────
    // UPDATE FEEDBACK ✅
    // ─────────────────────────────
    @Test
    void shouldUpdateFeedbackSuccessfully() {
        SessionFeedback updated = makeFeedback(1L, 5, "Mise à jour");

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(SessionFeedback.class))).thenReturn(updated);

        SessionFeedback result = feedbackService.updateFeedback(1L, updated);

        assertEquals(5, result.getRating());
        assertEquals("Mise à jour", result.getComment());
    }

    // ─────────────────────────────
    // UPDATE — RATING INVALIDE ❌
    // ─────────────────────────────
    @Test
    void shouldThrowWhenUpdatingWithInvalidRating() {
        SessionFeedback updated = makeFeedback(1L, 0, "Invalide");

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                feedbackService.updateFeedback(1L, updated)
        );

        assertTrue(ex.getMessage().contains("Rating must be between 1 and 5"));
    }

    // ─────────────────────────────
    // DELETE ✅
    // ─────────────────────────────
    @Test
    void shouldDeleteFeedback() {
        doNothing().when(feedbackRepository).deleteById(1L);

        feedbackService.deleteFeedback(1L);

        verify(feedbackRepository, times(1)).deleteById(1L);
    }
}