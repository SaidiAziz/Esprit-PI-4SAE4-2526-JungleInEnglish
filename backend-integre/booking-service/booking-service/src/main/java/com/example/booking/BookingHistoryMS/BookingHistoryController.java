package com.example.booking.BookingHistoryMS;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-history")

public class BookingHistoryController {

    private final BookingHistoryService historyService;

    public BookingHistoryController(BookingHistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * GET /api/booking-history/{id}
     * Récupérer un historique par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingHistory> getById(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.getById(id));
    }

    /**
     * GET /api/booking-history
     * Récupérer tout l'historique (Admin)
     */
    @GetMapping
    public ResponseEntity<List<BookingHistory>> getAll() {
        return ResponseEntity.ok(historyService.getAll());
    }

    /**
     * GET /api/booking-history/booking/{bookingId}
     * Historique d'une réservation (trié par date décroissante)
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingHistory>> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(historyService.getByBookingId(bookingId));
    }

    /**
     * GET /api/booking-history/by?changedBy=TUTOR
     * Historique filtré par qui a fait la modification
     */
    @GetMapping("/by")
    public ResponseEntity<List<BookingHistory>> getByChangedBy(
            @RequestParam String changedBy) {
        return ResponseEntity.ok(historyService.getByChangedBy(changedBy));
    }

    /**
     * GET /api/booking-history/status/{status}
     * Historique filtré par nouveau statut
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingHistory>> getByStatus(
            @PathVariable BookingStatusHistory status) {
        return ResponseEntity.ok(historyService.getByNewStatus(status));
    }

    /**
     * GET /api/booking-history/booking/{bookingId}/status/{status}
     * Historique d'une réservation avec un statut donné
     */
    @GetMapping("/booking/{bookingId}/status/{status}")
    public ResponseEntity<List<BookingHistory>> getByBookingAndStatus(
            @PathVariable Long bookingId,
            @PathVariable BookingStatusHistory status) {
        return ResponseEntity.ok(historyService.getByBookingAndStatus(bookingId, status));
    }

    /**
     * DELETE /api/booking-history/{id}
     * Supprimer un enregistrement d'historique
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        historyService.deleteHistory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/booking-history/booking/{bookingId}
     * Supprimer tout l'historique d'une réservation
     */
    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<Void> deleteAllByBooking(@PathVariable Long bookingId) {
        historyService.deleteAllByBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}
