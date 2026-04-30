package com.example.booking.SessionFeedbackMS;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final SessionFeedbackService feedbackService;



    public FeedbackController(SessionFeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // ── CREATE ──────────────────────────────────────

    /**
     * Ajouter un feedback après une session
     * POST /api/feedback/session/{sessionId}
     */
    @PostMapping("/session/{sessionId}")
    public ResponseEntity<SessionFeedback> addFeedback(
            @PathVariable Long sessionId,
            @RequestBody SessionFeedback feedback) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(feedbackService.addFeedback(sessionId, feedback));
    }

    // ── READ ─────────────────────────────────────────

    /**
     * Récupérer un feedback par ID
     * GET /api/feedback/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionFeedback> getById(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getById(id));
    }

    /**
     * Récupérer le feedback d'une session
     * GET /api/feedback/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<SessionFeedback> getBySessionId(@PathVariable Long sessionId) {
        return ResponseEntity.ok(feedbackService.getBySessionId(sessionId));
    }

    /**
     * Récupérer tous les feedbacks (admin)
     * GET /api/feedback
     */
    @GetMapping
    public ResponseEntity<List<SessionFeedback>> getAll() {
        return ResponseEntity.ok(feedbackService.getAll());
    }

    /**
     * Récupérer les feedbacks d'un étudiant
     * GET /api/feedback/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SessionFeedback>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(feedbackService.getByStudent(studentId));
    }

    /**
     * Récupérer les feedbacks d'un tuteur
     * GET /api/feedback/tutor/{tutorId}
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<SessionFeedback>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(feedbackService.getByTutor(tutorId));
    }

    /**
     * Note moyenne d'un tuteur
     * GET /api/feedback/tutor/{tutorId}/average
     */
    @GetMapping("/tutor/{tutorId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long tutorId) {
        return ResponseEntity.ok(feedbackService.getAverageRatingByTutor(tutorId));
    }

    /**
     * Feedbacks avec note minimale
     * GET /api/feedback/rating?min=4
     */
    @GetMapping("/rating")
    public ResponseEntity<List<SessionFeedback>> getByMinRating(
            @RequestParam Integer min) {
        return ResponseEntity.ok(feedbackService.getByMinRating(min));
    }

    // ── UPDATE ───────────────────────────────────────

    /**
     * Modifier un feedback
     * PUT /api/feedback/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SessionFeedback> updateFeedback(
            @PathVariable Long id,
            @RequestBody SessionFeedback feedback) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, feedback));
    }

    // ── DELETE ───────────────────────────────────────

    /**
     * Supprimer un feedback
     * DELETE /api/feedback/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}