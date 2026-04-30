package com.example.booking.SessionMS;

import com.example.booking.BookingMS.Booking;
import com.example.booking.SessionFeedbackMS.SessionFeedback;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id")
    @JsonIgnore
    private Booking booking;

    private String meetingLink;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    @JsonIgnore
    private SessionFeedback feedback;

    // ── Helper : reconstruit le scheduledAt depuis le Booking ────────
    public LocalDateTime getScheduledAt() {
        if (booking == null) return null;
        if (booking.getSessionDate() == null || booking.getStartTime() == null) return null;
        return LocalDateTime.of(booking.getSessionDate(), booking.getStartTime());
    }

    // ── Helper : reconstruit le scheduledEnd depuis le Booking ───────
    public LocalDateTime getScheduledEnd() {
        if (booking == null) return null;
        if (booking.getSessionDate() == null || booking.getEndTime() == null) return null;
        return LocalDateTime.of(booking.getSessionDate(), booking.getEndTime());
    }

    // ── Getters / Setters ────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public SessionFeedback getFeedback() { return feedback; }
    public void setFeedback(SessionFeedback feedback) { this.feedback = feedback; }
}