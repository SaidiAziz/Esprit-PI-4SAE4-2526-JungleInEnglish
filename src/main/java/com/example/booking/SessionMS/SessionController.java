package com.example.booking.SessionMS;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // ── CREATE ───────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Session> createSession(@Valid @RequestBody Session session) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSession(session));
    }

    // ── READ ─────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Session> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getById(id));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Session> getByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(sessionService.getByBookingId(bookingId));
    }

    @GetMapping
    public ResponseEntity<List<Session>> getAll() {
        return ResponseEntity.ok(sessionService.getAll());
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<Session>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(sessionService.getByTutor(tutorId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Session>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(sessionService.getByStudent(studentId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Session>> getByStatus(@PathVariable SessionStatus status) {
        return ResponseEntity.ok(sessionService.getByStatus(status));
    }

    @GetMapping("/tutor/{tutorId}/status/{status}")
    public ResponseEntity<List<Session>> getByTutorAndStatus(
            @PathVariable Long tutorId,
            @PathVariable SessionStatus status) {
        return ResponseEntity.ok(sessionService.getByTutorAndStatus(tutorId, status));
    }

    @GetMapping("/tutor/{tutorId}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long tutorId) {
        return ResponseEntity.ok(sessionService.getAverageRatingByTutor(tutorId));
    }

    // ── UPDATE ───────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Session> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody Session session) {
        return ResponseEntity.ok(sessionService.updateSession(id, session));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<Session> startSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.startSession(id));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<Session> endSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.endSession(id));
    }

    @PatchMapping("/{id}/missed")
    public ResponseEntity<Session> markAsMissed(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.markAsMissed(id));
    }

    // ── DELETE ───────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    // ── DASHBOARDS ───────────────────────────────────────────────────
    @GetMapping("dashboard/student/{studentId}")
    public ResponseEntity<StudentDashboardDTO> getStudentDashboard(@PathVariable Long studentId) {
        return ResponseEntity.ok(sessionService.getStudentDashboard(studentId));
    }

    @GetMapping("dashboard/tutor/{tutorId}")
    public ResponseEntity<TutorDashboardDTO> getTutorDashboard(@PathVariable Long tutorId) {
        return ResponseEntity.ok(sessionService.getTutorDashboard(tutorId));
    }

    // ── EXCEPTION HANDLER ────────────────────────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBadTransition(IllegalStateException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

}