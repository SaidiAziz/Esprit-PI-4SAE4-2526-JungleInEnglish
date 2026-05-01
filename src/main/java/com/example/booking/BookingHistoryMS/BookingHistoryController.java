package com.example.booking.BookingHistoryMS;

import com.example.booking.BookingMS.BookingStatus;
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

    @GetMapping("/{id}")
    public ResponseEntity<BookingHistory> getById(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<BookingHistory>> getAll() {
        return ResponseEntity.ok(historyService.getAll());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingHistory>> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(historyService.getByBookingId(bookingId));
    }

    @GetMapping("/by")
    public ResponseEntity<List<BookingHistory>> getByChangedBy(@RequestParam String changedBy) {
        return ResponseEntity.ok(historyService.getByChangedBy(changedBy));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingHistory>> getByStatus(
            @PathVariable BookingStatus status) { // ✅
        return ResponseEntity.ok(historyService.getByNewStatus(status));
    }

    @GetMapping("/booking/{bookingId}/status/{status}")
    public ResponseEntity<List<BookingHistory>> getByBookingAndStatus(
            @PathVariable Long bookingId,
            @PathVariable BookingStatus status) { // ✅
        return ResponseEntity.ok(historyService.getByBookingAndStatus(bookingId, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        historyService.deleteHistory(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<Void> deleteAllByBooking(@PathVariable Long bookingId) {
        historyService.deleteAllByBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}