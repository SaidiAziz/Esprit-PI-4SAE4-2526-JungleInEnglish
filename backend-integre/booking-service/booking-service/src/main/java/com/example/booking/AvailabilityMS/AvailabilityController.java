package com.example.booking.AvailabilityMS;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * POST /api/availability
     * Ajouter un créneau de disponibilité
     */
    @PostMapping
    public ResponseEntity<Availability> addAvailability(
            @Valid @RequestBody Availability availability) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.addAvailability(availability));
    }

    /**
     * GET /api/availability/{id}
     * Récupérer une disponibilité par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Availability> getById(@PathVariable Long id) {
        return ResponseEntity.ok(availabilityService.getById(id));
    }

    /**
     * GET /api/availability
     * Récupérer toutes les disponibilités
     */
    @GetMapping
    public ResponseEntity<List<Availability>> getAll() {
        return ResponseEntity.ok(availabilityService.getAll());
    }

    /**
     * GET /api/availability/tutor/{tutorId}
     * Récupérer toutes les disponibilités d'un tuteur
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<Availability>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(availabilityService.getByTutor(tutorId));
    }

    /**
     * GET /api/availability/tutor/{tutorId}/available
     * Récupérer les créneaux disponibles d'un tuteur
     */
    @GetMapping("/tutor/{tutorId}/available")
    public ResponseEntity<List<Availability>> getAvailableByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(availabilityService.getAvailableSlotsByTutor(tutorId));
    }

    /**
     * GET /api/availability/tutor/{tutorId}/day?day=MONDAY
     * Récupérer disponibilités par jour de la semaine
     */
    @GetMapping("/tutor/{tutorId}/day")
    public ResponseEntity<List<Availability>> getByTutorAndDay(
            @PathVariable Long tutorId,
            @RequestParam DayOfWeek day) {
        return ResponseEntity.ok(availabilityService.getByTutorAndDay(tutorId,day));
    }

    /**
     * GET /api/availability/tutor/{tutorId}/date?date=2026-03-01
     * Récupérer disponibilités par date spécifique
     */
    @GetMapping("/tutor/{tutorId}/date")
    public ResponseEntity<List<Availability>> getByTutorAndDate(
            @PathVariable Long tutorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getByTutorAndDate(tutorId, date));
    }

    /**
     * GET /api/availability/tutor/{tutorId}/slots?day=MONDAY&start=09:00&end=10:00
     * Vérifier si un créneau est disponible
     */
    @GetMapping("/tutor/{tutorId}/slots")
    public ResponseEntity<List<Availability>> getAvailableSlots(
            @PathVariable Long tutorId,
            @RequestParam DayOfWeek day,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end) {
        return ResponseEntity.ok(availabilityService.getAvailableSlots(tutorId, day, start, end));
    }

    /**
     * PUT /api/availability/{id}
     * Modifier une disponibilité
     */
    @PutMapping("/{id}")
    public ResponseEntity<Availability> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody Availability availability) {
        return ResponseEntity.ok(availabilityService.updateAvailability(id, availability));
    }

    /**
     * PATCH /api/availability/{id}/toggle
     * Activer / désactiver un créneau
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Availability> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(availabilityService.toggleAvailability(id));
    }

    /**
     * DELETE /api/availability/{id}
     * Supprimer une disponibilité
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/availability/tutor/{tutorId}
     * Supprimer toutes les disponibilités d'un tuteur
     */
    @DeleteMapping("/tutor/{tutorId}")
    public ResponseEntity<Void> deleteAllByTutor(@PathVariable Long tutorId) {
        availabilityService.deleteAllByTutor(tutorId);
        return ResponseEntity.noContent().build();
    }
}
