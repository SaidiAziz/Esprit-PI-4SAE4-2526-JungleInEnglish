package com.example.booking.SessionMS;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // ── Transitions autorisées ───────────────────────────────────────
    private static final Map<SessionStatus, Set<SessionStatus>> ALLOWED = Map.of(
            SessionStatus.SCHEDULED, Set.of(SessionStatus.ONGOING, SessionStatus.MISSED),
            SessionStatus.ONGOING,   Set.of(SessionStatus.DONE),
            SessionStatus.DONE,      Set.of(),
            SessionStatus.MISSED,    Set.of()
    );

    private Session doTransition(Long id, SessionStatus newStatus) {
        Session session = getById(id);
        SessionStatus current = session.getStatus();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledAt = session.getScheduledAt();
        LocalDateTime scheduledEnd = session.getScheduledEnd();

        // 1. Vérifier la transition de statut
        if (!ALLOWED.get(current).contains(newStatus)) {
            throw new IllegalStateException(
                    "Transition interdite : " + current + " → " + newStatus
            );
        }

        // 2. Vérifier les conditions temporelles
        if (scheduledAt != null) {
            switch (newStatus) {

                case ONGOING -> {
                    // Ne peut pas démarrer avant l'heure prévue
                    if (now.isBefore(scheduledAt)) {
                        throw new IllegalStateException(
                                "Impossible de démarrer avant l'heure prévue : " + scheduledAt
                        );
                    }
                }

                case DONE -> {
                    // Ne peut pas terminer avant la fin prévue
                    if (scheduledEnd != null && now.isBefore(scheduledEnd)) {
                        throw new IllegalStateException(
                                "La session ne peut pas se terminer avant " + scheduledEnd
                        );
                    } else if (scheduledEnd == null
                            && session.getStartedAt() != null
                            && now.isBefore(session.getStartedAt().plusMinutes(5))) {
                        throw new IllegalStateException(
                                "La session doit durer au moins 5 minutes"
                        );
                    }
                }

                case MISSED -> {
                    // Ne peut être marquée manquée qu'après 15 min de délai de grâce
                    if (now.isBefore(scheduledAt.plusMinutes(15))) {
                        throw new IllegalStateException(
                                "La session ne peut être marquée manquée qu'après "
                                        + scheduledAt.plusMinutes(15)
                        );
                    }
                }

                default -> {
                    // No additional time constraints for this status
                }
            }
        }

        return session;
    }

    // ── CREATE ───────────────────────────────────────────────────────
    public Session createSession(Session session) {
        session.setStatus(SessionStatus.SCHEDULED);
        return sessionRepository.save(session);
    }

    // ── READ ─────────────────────────────────────────────────────────
    public Session getById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
    }

    public Session getByBookingId(Long bookingId) {
        return sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Session not found for booking: " + bookingId));
    }

    public List<Session> getAll() {
        return sessionRepository.findAll();
    }

    public List<Session> getByTutor(Long tutorId) {
        return sessionRepository.findByTutorId(tutorId);
    }

    public List<Session> getByStudent(Long studentId) {
        return sessionRepository.findByStudentId(studentId);
    }

    public List<Session> getByStatus(SessionStatus status) {
        return sessionRepository.findByStatus(status);
    }

    public List<Session> getByTutorAndStatus(Long tutorId, SessionStatus status) {
        return sessionRepository.findByTutorIdAndStatus(tutorId, status);
    }

    public Double getAverageRatingByTutor(Long tutorId) {
        return sessionRepository.getAverageRatingByTutor(tutorId);
    }

    // ── UPDATE ───────────────────────────────────────────────────────
    @Transactional
    public Session updateSession(Long id, Session updated) {
        Session existing = getById(id);
        existing.setMeetingLink(updated.getMeetingLink());
        existing.setDuration(updated.getDuration());
        return sessionRepository.save(existing);
    }

    @Transactional
    public Session startSession(Long id) {
        Session session = doTransition(id, SessionStatus.ONGOING);
        session.setStatus(SessionStatus.ONGOING);
        session.setStartedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    @Transactional
    public Session endSession(Long id) {
        Session session = doTransition(id, SessionStatus.DONE);
        session.setStatus(SessionStatus.DONE);
        session.setEndedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    @Transactional
    public Session markAsMissed(Long id) {
        Session session = doTransition(id, SessionStatus.MISSED);
        session.setStatus(SessionStatus.MISSED);
        session.setEndedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // ── DELETE ───────────────────────────────────────────────────────
    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }

    // ── DASHBOARDS ───────────────────────────────────────────────────
    public StudentDashboardDTO getStudentDashboard(Long studentId) {
        StudentDashboardDTO dto = new StudentDashboardDTO();
        dto.setTotalSessions(sessionRepository.countByStudentId(studentId));
        dto.setUpcomingSessions(sessionRepository.countByStudentIdAndStatus(studentId, SessionStatus.SCHEDULED));
        dto.setTotalMinutesLearned(sessionRepository.getTotalMinutesByStudent(studentId));
        dto.setNextSessions(sessionRepository.findByStudentIdAndStatus(studentId, SessionStatus.SCHEDULED));
        dto.setSessionHistory(sessionRepository.findByStudentIdAndStatus(studentId, SessionStatus.DONE));
        return dto;
    }

    public TutorDashboardDTO getTutorDashboard(Long tutorId) {
        TutorDashboardDTO dto = new TutorDashboardDTO();
        dto.setTotalSessions(sessionRepository.countByTutorId(tutorId));
        dto.setUpcomingSessions(sessionRepository.countByTutorIdAndStatus(tutorId, SessionStatus.SCHEDULED));
        Double avg = sessionRepository.getAverageRatingByTutor(tutorId);
        dto.setAverageRating(avg != null ? avg : 0.0);
        dto.setPlannedSessions(sessionRepository.findByTutorIdAndStatus(tutorId, SessionStatus.SCHEDULED));
        return dto;
    }
}