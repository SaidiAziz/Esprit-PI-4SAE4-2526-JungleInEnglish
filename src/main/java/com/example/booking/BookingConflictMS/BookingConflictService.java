package com.example.booking.BookingConflictMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingConflictService {

    // ✅ @Autowired SANS final SANS constructeur
    @Autowired
    private BookingConflictsRepository conflictRepository;

    // ── CREATE ──────────────────────────────────────
    public BookingConflict saveConflict(Long tutorId, LocalDate date,
                                        String startTime, String endTime, String reason) {
        BookingConflict conflict = new BookingConflict();
        conflict.setTutorId(tutorId);
        conflict.setConflictDate(date);
        conflict.setReason(reason);
        conflict.setDetectedAt(LocalDateTime.now());
        return conflictRepository.save(conflict);
    }

    // ── READ ─────────────────────────────────────────
    public BookingConflict getById(Long id) {
        return conflictRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conflict not found with id: " + id));
    }

    public List<BookingConflict> getAll() {
        return conflictRepository.findAll();
    }

    public List<BookingConflict> getByTutor(Long tutorId) {
        return conflictRepository.findByTutorId(tutorId);
    }

    public List<BookingConflict> getByDate(LocalDate date) {
        return conflictRepository.findByConflictDate(date);
    }

    public List<BookingConflict> getByTutorAndDate(Long tutorId, LocalDate date) {
        return conflictRepository.findByTutorIdAndConflictDate(tutorId, date);
    }

    public List<BookingConflict> getRecentByTutor(Long tutorId) {
        return conflictRepository.findRecentConflictsByTutor(tutorId);
    }

    // ── DELETE ───────────────────────────────────────
    public void deleteConflict(Long id) {
        conflictRepository.deleteById(id);
    }

    public void deleteAllByTutor(Long tutorId) {
        conflictRepository.deleteByTutorId(tutorId);
    }
}