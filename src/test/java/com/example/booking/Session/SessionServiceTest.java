package com.example.booking.Session;

import com.example.booking.BookingMS.Booking;
import com.example.booking.SessionMS.Session;
import com.example.booking.SessionMS.SessionRepository;
import com.example.booking.SessionMS.SessionService;
import com.example.booking.SessionMS.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionService sessionService;

    private Session session;

    // ── Helpers pour construire un Booking avec les bonnes heures ────
    private Booking bookingWithTimes(LocalDateTime scheduledAt, LocalDateTime scheduledEnd) {
        Booking booking = new Booking();
        booking.setSessionDate(scheduledAt.toLocalDate());
        booking.setStartTime(scheduledAt.toLocalTime());
        if (scheduledEnd != null) {
            booking.setEndTime(scheduledEnd.toLocalTime());
        }
        return booking;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new Session();
        session.setId(1L);
        session.setStatus(SessionStatus.SCHEDULED);
    }

    // ─────────────────────────────
    // START SESSION ✅
    // ─────────────────────────────
    @Test
    void shouldStartSessionSuccessfully() {
        // scheduledAt dans le passé → autorisé
        session.setBooking(bookingWithTimes(
                LocalDateTime.now().minusMinutes(5), null
        ));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        Session result = sessionService.startSession(1L);

        assertEquals(SessionStatus.ONGOING, result.getStatus());
        assertNotNull(result.getStartedAt());
        verify(sessionRepository, times(1)).save(session);
    }

    // ─────────────────────────────
    // START TOO EARLY ❌
    // ─────────────────────────────
    @Test
    void shouldThrowExceptionWhenStartingTooEarly() {
        // scheduledAt dans le futur → interdit
        session.setBooking(bookingWithTimes(
                LocalDateTime.now().plusHours(2), null
        ));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                sessionService.startSession(1L)
        );

        assertTrue(exception.getMessage().contains("Impossible de démarrer"));
    }

    // ─────────────────────────────
    // END SESSION ✅
    // ─────────────────────────────
    @Test
    void shouldEndSessionSuccessfully() {
        // scheduledEnd dans le passé → autorisé
        session.setStatus(SessionStatus.ONGOING);
        session.setStartedAt(LocalDateTime.now().minusHours(1));
        session.setBooking(bookingWithTimes(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().minusMinutes(5)  // scheduledEnd passé
        ));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        Session result = sessionService.endSession(1L);

        assertEquals(SessionStatus.DONE, result.getStatus());
        assertNotNull(result.getEndedAt());
    }

    // ─────────────────────────────
    // MARK AS MISSED ✅
    // ─────────────────────────────
    @Test
    void shouldMarkSessionAsMissed() {
        // scheduledAt + 15 min dépassé → autorisé
        session.setBooking(bookingWithTimes(
                LocalDateTime.now().minusMinutes(20), null
        ));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        Session result = sessionService.markAsMissed(1L);

        assertEquals(SessionStatus.MISSED, result.getStatus());
    }

    // ─────────────────────────────
    // MARK AS MISSED TOO EARLY ❌
    // ─────────────────────────────
    @Test
    void shouldThrowExceptionWhenMarkingMissedTooEarly() {
        // scheduledAt + 15 min pas encore dépassé → interdit
        session.setBooking(bookingWithTimes(
                LocalDateTime.now().minusMinutes(5), null
        ));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                sessionService.markAsMissed(1L)
        );

        assertTrue(exception.getMessage().contains("marquée manquée"));
    }

    // ─────────────────────────────
    // CREATE SESSION ✅
    // ─────────────────────────────
    @Test
    void shouldCreateSessionWithScheduledStatus() {
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        Session result = sessionService.createSession(session);

        assertEquals(SessionStatus.SCHEDULED, result.getStatus());
    }

    // ─────────────────────────────
    // NOT FOUND ❌
    // ─────────────────────────────
    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                sessionService.getById(1L)
        );
    }

    // ─────────────────────────────
    // TRANSITION INTERDITE ❌
    // ─────────────────────────────
    @Test
    void shouldThrowExceptionOnForbiddenTransition() {
        // DONE → ONGOING : transition interdite
        session.setStatus(SessionStatus.DONE);
        session.setBooking(bookingWithTimes(
                LocalDateTime.now().minusMinutes(5), null
        ));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                sessionService.startSession(1L)
        );

        assertTrue(exception.getMessage().contains("Transition interdite"));
    }
}