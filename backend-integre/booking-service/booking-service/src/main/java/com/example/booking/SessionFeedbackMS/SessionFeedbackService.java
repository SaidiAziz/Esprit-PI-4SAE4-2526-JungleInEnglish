package com.example.booking.SessionFeedbackMS;

import com.example.booking.SessionMS.Session;
import com.example.booking.SessionMS.SessionRepository;
import com.example.booking.SessionMS.SessionStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class SessionFeedbackService {
    @Autowired

    private final SessionFeedbackRepository feedbackRepository;
    @Autowired

    private final SessionRepository sessionRepository;

    public SessionFeedbackService(SessionFeedbackRepository feedbackRepository, SessionRepository sessionRepository) {
        this.feedbackRepository = feedbackRepository;
        this.sessionRepository = sessionRepository;
    }

    // ── CREATE ──────────────────────────────────────
    @Transactional
    public SessionFeedback addFeedback(Long sessionId, SessionFeedback feedback) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.DONE) {
            throw new RuntimeException("Cannot rate a session that is not completed yet");
        }

        if (feedbackRepository.existsBySessionId(sessionId)) {
            throw new RuntimeException("Feedback already exists for this session");
        }

        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        feedback.setSession(session);
        feedback.setCreatedAt(LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }

    // ── READ ─────────────────────────────────────────
    public SessionFeedback getById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
    }

    public SessionFeedback getBySessionId(Long sessionId) {
        return feedbackRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("No feedback for session: " + sessionId));
    }

    public List<SessionFeedback> getAll() {
        return feedbackRepository.findAll();
    }

    public List<SessionFeedback> getByStudent(Long studentId) {
        return feedbackRepository.findByStudentId(studentId);
    }

    public List<SessionFeedback> getByTutor(Long tutorId) {
        return feedbackRepository.findByTutorId(tutorId);
    }

    public Double getAverageRatingByTutor(Long tutorId) {
        return feedbackRepository.getAverageRatingByTutor(tutorId);
    }

    public List<SessionFeedback> getByMinRating(Integer minRating) {
        return feedbackRepository.findByRatingGreaterThanEqual(minRating);
    }

    // ── UPDATE ───────────────────────────────────────
    @Transactional
    public SessionFeedback updateFeedback(Long id, SessionFeedback updated) {
        SessionFeedback existing = getById(id);

        if (updated.getRating() < 1 || updated.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        existing.setRating(updated.getRating());
        existing.setComment(updated.getComment());
        return feedbackRepository.save(existing);
    }

    // ── DELETE ───────────────────────────────────────
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}