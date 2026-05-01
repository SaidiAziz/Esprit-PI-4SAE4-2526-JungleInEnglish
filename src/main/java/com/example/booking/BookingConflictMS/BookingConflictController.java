package com.example.booking.BookingConflictMS;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/booking-conflicts")

public class BookingConflictController {

    private final BookingConflictService conflictService;

    public BookingConflictController(BookingConflictService conflictService) {
        this.conflictService = conflictService;
    }

    /**
     * GET /api/booking-conflicts/{id}
     * Récupérer un conflit par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingConflict> getById(@PathVariable Long id) {
        return ResponseEntity.ok(conflictService.getById(id));
    }

    /**
     * GET /api/booking-conflicts
     * Récupérer tous les conflits (Admin)
     */
    @GetMapping
    public ResponseEntity<List<BookingConflict>> getAll() {
        return ResponseEntity.ok(conflictService.getAll());
    }

    /**
     * GET /api/booking-conflicts/tutor/{tutorId}
     * Conflits d'un tuteur
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<BookingConflict>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(conflictService.getByTutor(tutorId));
    }

    /**
     * GET /api/booking-conflicts/tutor/{tutorId}/recent
     * Conflits récents d'un tuteur (triés par date)
     */
    @GetMapping("/tutor/{tutorId}/recent")
    public ResponseEntity<List<BookingConflict>> getRecentByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(conflictService.getRecentByTutor(tutorId));
    }

    /**
     * GET /api/booking-conflicts/date?date=2026-03-01
     * Conflits par date
     */
    @GetMapping("/date")
    public ResponseEntity<List<BookingConflict>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(conflictService.getByDate(date));
    }

    /**
     * GET /api/booking-conflicts/tutor/{tutorId}/date?date=2026-03-01
     * Conflits d'un tuteur à une date donnée
     */
    @GetMapping("/tutor/{tutorId}/date")
    public ResponseEntity<List<BookingConflict>> getByTutorAndDate(
            @PathVariable Long tutorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(conflictService.getByTutorAndDate(tutorId, date));
    }

    /**
     * DELETE /api/booking-conflicts/{id}
     * Supprimer un conflit
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConflict(@PathVariable Long id) {
        conflictService.deleteConflict(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/booking-conflicts/tutor/{tutorId}
     * Supprimer tous les conflits d'un tuteur
     */
    @DeleteMapping("/tutor/{tutorId}")
    public ResponseEntity<Void> deleteAllByTutor(@PathVariable Long tutorId) {
        conflictService.deleteAllByTutor(tutorId);
        return ResponseEntity.noContent().build();
    }
}
