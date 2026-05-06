package com.example.booking.BookingMS;

import com.example.booking.AvailabilityMS.Availability;
import com.example.booking.SessionFeedbackMS.SessionFeedback;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/bookings")


public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ─────────────────────────────────────────────
    //  BOOKING CRUD
    // ─────────────────────────────────────────────

    /**
     * Créer une réservation
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody Booking booking) {
        Booking created = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupérer une réservation par ID
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /**
     * Récupérer toutes les réservations (Admin)
     * GET /api/bookings
     */
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * Récupérer les réservations d'un étudiant
     * GET /api/bookings/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Booking>> getBookingsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(bookingService.getBookingsByStudent(studentId));
    }

    /**
     * Récupérer les réservations d'un tuteur
     * GET /api/bookings/tutor/{tutorId}
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<Booking>> getBookingsByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(bookingService.getBookingsByTutor(tutorId));
    }

    /**
     * Modifier une réservation
     * PUT /api/bookings/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody Booking booking) {
        return ResponseEntity.ok(bookingService.updateBooking(id, booking));
    }

    /**
     * Supprimer une réservation
     * DELETE /api/bookings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────
    //  GESTION DES STATUTS
    // ─────────────────────────────────────────────

    /**
     * Tuteur confirme la réservation
     * PUT /api/bookings/{id}/confirm
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    /**
     * Tuteur refuse la réservation
     * PUT /api/bookings/{id}/reject
     */
    // BookingController.java
    @PutMapping("/{id}/reject")
    public ResponseEntity<Booking> rejectBooking(
            @PathVariable Long id,
            @RequestParam String reason) {                    // ← @RequestParam pas @RequestBody
        return ResponseEntity.ok(bookingService.rejectBooking(id, reason));
    }

    /**
     * Annuler une réservation
     * PUT /api/bookings/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            @RequestParam String cancelledBy,
            @RequestParam(required = false, defaultValue = "Cancelled") String reason) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, cancelledBy, reason));
    }

    /**
     * Marquer une réservation comme terminée
     * PUT /api/bookings/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Booking> completeBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.completeBooking(id));
    }

    // ─────────────────────────────────────────────
    //  AVAILABILITY CRUD
    // ─────────────────────────────────────────────

    /**
     * Tuteur ajoute un créneau de disponibilité
     * POST /api/bookings/availability
     */
    @PostMapping("/availability")
    public ResponseEntity<Availability> addAvailability(
            @Valid @RequestBody Availability availability) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.addAvailability(availability));
    }

    /**
     * Récupérer les disponibilités d'un tuteur
     * GET /api/bookings/availability/tutor/{tutorId}
     */
    @GetMapping("/availability/tutor/{tutorId}")
    public ResponseEntity<List<Availability>> getAvailabilityByTutor(
            @PathVariable Long tutorId) {
        return ResponseEntity.ok(bookingService.getAvailabilityByTutor(tutorId));
    }

    /**
     * Modifier une disponibilité
     * PUT /api/bookings/availability/{id}
     */
    @PutMapping("/availability/{id}")
    public ResponseEntity<Availability> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody Availability availability) {
        return ResponseEntity.ok(bookingService.updateAvailability(id, availability));
    }

    /**
     * Supprimer une disponibilité
     * DELETE /api/bookings/availability/{id}
     */
    @DeleteMapping("/availability/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        bookingService.deleteAvailability(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────
    //  SESSION FEEDBACK CRUD
    // ─────────────────────────────────────────────

    /**
     * Ajouter un feedback après une session
     * POST /api/bookings/sessions/{sessionId}/feedback
     */
    @PostMapping("/sessions/{sessionId}/feedback")
    public ResponseEntity<SessionFeedback> addFeedback(
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionFeedback feedback) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.addFeedback(sessionId, feedback));
    }

    /**
     * Récupérer les feedbacks d'un tuteur
     * GET /api/bookings/sessions/feedback/tutor/{tutorId}
     */
    @GetMapping("/sessions/feedback/tutor/{tutorId}")
    public ResponseEntity<List<SessionFeedback>> getFeedbackByTutor(
            @PathVariable Long tutorId) {
        return ResponseEntity.ok(bookingService.getFeedbackByTutor(tutorId));
    }
}
