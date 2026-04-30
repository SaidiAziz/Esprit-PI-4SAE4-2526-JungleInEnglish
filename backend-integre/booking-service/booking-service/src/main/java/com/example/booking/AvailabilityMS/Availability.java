package com.example.booking.AvailabilityMS;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "availability")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tutorId;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;   // ✅ java.time.DayOfWeek (MONDAY, TUESDAY...)

    private LocalTime startTime;

    private LocalTime endTime;

    // ✅ boolean standard — getter = isAvailable(), setter = setAvailable()
    private boolean available;

    @Enumerated(EnumType.STRING)
    private AvailabilityType availabilityType; // RECURRING, ONE_TIME

    private LocalDate specificDate; // utilisé si ONE_TIME

    // ─── Constructeurs ───────────────────────────
    public Availability() {}

    // ─── Getters & Setters ───────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    // ✅ getter boolean standard → isAvailable()
    public boolean isAvailable() { return available; }

    // ✅ setter boolean standard → setAvailable()
    public void setAvailable(boolean available) { this.available = available; }

    public AvailabilityType getAvailabilityType() { return availabilityType; }
    public void setAvailabilityType(AvailabilityType availabilityType) {
        this.availabilityType = availabilityType;
    }

    public LocalDate getSpecificDate() { return specificDate; }
    public void setSpecificDate(LocalDate specificDate) { this.specificDate = specificDate; }
}