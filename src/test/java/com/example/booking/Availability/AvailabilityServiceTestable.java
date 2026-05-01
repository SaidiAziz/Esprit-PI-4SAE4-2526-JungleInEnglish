package com.example.booking.Availability;

// src/test/java/com/example/booking/AvailabilityMS/AvailabilityServiceTestable.java


import com.example.booking.AvailabilityMS.Availability;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AvailabilityServiceTestable {

    private final TestAvailabilityRepository repo;

    public AvailabilityServiceTestable(TestAvailabilityRepository repo) {
        this.repo = repo;
    }

    public Availability addAvailability(Availability availability) {
        return repo.save(availability);
    }

    public Availability getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability not found with id: " + id));
    }

    public List<Availability> getAll() {
        return repo.findAll();
    }

    public List<Availability> getByTutor(Long tutorId) {
        return repo.findByTutorId(tutorId);
    }

    public List<Availability> getAvailableSlotsByTutor(Long tutorId) {
        return repo.findByTutorIdAndAvailableTrue(tutorId);
    }

    public List<Availability> getByTutorAndDay(Long tutorId, DayOfWeek day) {
        return repo.findByTutorIdAndDayOfWeek(tutorId, day);
    }

    public List<Availability> getByTutorAndDate(Long tutorId, LocalDate date) {
        return repo.findByTutorIdAndSpecificDate(tutorId, date);
    }

    public List<Availability> getAvailableSlots(Long tutorId, DayOfWeek day,
                                                LocalTime start, LocalTime end) {
        return repo.findAvailableSlots(tutorId, day, start, end);
    }

    public Availability updateAvailability(Long id, Availability updated) {
        Availability existing = getById(id);
        existing.setDayOfWeek(updated.getDayOfWeek());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setAvailable(updated.isAvailable());
        existing.setAvailabilityType(updated.getAvailabilityType());
        existing.setSpecificDate(updated.getSpecificDate());
        return repo.save(existing);
    }

    public Availability toggleAvailability(Long id) {
        Availability availability = getById(id);
        availability.setAvailable(!availability.isAvailable());
        return repo.save(availability);
    }

    public void deleteAvailability(Long id) {
        repo.deleteById(id);
    }

    public void deleteAllByTutor(Long tutorId) {
        repo.deleteByTutorId(tutorId);
    }
}