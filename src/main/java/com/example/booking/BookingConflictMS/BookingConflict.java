package com.example.booking.BookingConflictMS;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "booking_conflict")
public class BookingConflict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tutorId;

    private LocalDate conflictDate;
    private LocalTime conflictStart;
    private LocalTime conflictEnd;

    private String reason;

    private LocalDateTime detectedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTutorId() {
        return tutorId;
    }

    public void setTutorId(Long tutorId) {
        this.tutorId = tutorId;
    }

    public LocalDate getConflictDate() {
        return conflictDate;
    }

    public void setConflictDate(LocalDate conflictDate) {
        this.conflictDate = conflictDate;
    }

    public LocalTime getConflictStart() {
        return conflictStart;
    }

    public void setConflictStart(LocalTime conflictStart) {
        this.conflictStart = conflictStart;
    }

    public LocalTime getConflictEnd() {
        return conflictEnd;
    }

    public void setConflictEnd(LocalTime conflictEnd) {
        this.conflictEnd = conflictEnd;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}