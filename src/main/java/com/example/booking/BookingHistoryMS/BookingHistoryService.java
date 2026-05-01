package com.example.booking.BookingHistoryMS;

import com.example.booking.BookingMS.BookingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingHistoryService {
    @Autowired

    private final BookingHistoryRepository historyRepository;

    public BookingHistoryService(BookingHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    // ── CREATE ──────────────────────────────────────
    public BookingHistory save(BookingHistory history) {
        return historyRepository.save(history);
    }

    // ── READ ─────────────────────────────────────────
    public BookingHistory getById(Long id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("History not found with id: " + id));
    }

    public List<BookingHistory> getAll() {
        return historyRepository.findAll();
    }

    public List<BookingHistory> getByBookingId(Long bookingId) {
        return historyRepository.findByBookingIdOrderByChangedAtDesc(bookingId);
    }

    public List<BookingHistory> getByChangedBy(String changedBy) {
        return historyRepository.findByChangedBy(changedBy);
    }

    public List<BookingHistory> getByNewStatus(BookingStatus status) { // ✅
        return historyRepository.findByNewStatus(status);
    }
    public List<BookingHistory> getByBookingAndStatus(Long bookingId, BookingStatus status) { // ✅
        return historyRepository.findByBookingIdAndNewStatus(bookingId, status);
    }

    // ── DELETE ───────────────────────────────────────
    public void deleteHistory(Long id) {
        historyRepository.deleteById(id);
    }

    public void deleteAllByBooking(Long bookingId) {
        historyRepository.findByBookingId(bookingId)
                .forEach(h -> historyRepository.deleteById(h.getId()));
    }
}