package com.example.booking.AvailabilityMS;

import com.example.booking.AvailabilityMS.Availability;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service

public class AvailabilityService {
@Autowired
    private final AvailabilityRepository availabilityRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    // ── CREATE ──────────────────────────────────────
    public Availability addAvailability(Availability availability) {
        return availabilityRepository.save(availability);
    }

    // ── READ ─────────────────────────────────────────
    public Availability getById(Long id) {
        return availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Availability not found with id: " + id));
    }

    public List<Availability> getAll() {
        return availabilityRepository.findAll();
    }

    public List<Availability> getByTutor(Long tutorId) {
        return availabilityRepository.findByTutorId(tutorId);
    }

    public List<Availability> getAvailableSlotsByTutor(Long tutorId) {
        return availabilityRepository.findByTutorIdAndAvailableIsTrue(tutorId);
    }

    public List<Availability> getByTutorAndDay(Long tutorId, DayOfWeek day) {
        return availabilityRepository.findByTutorIdAndDayOfWeek(tutorId, day);
    }

    public List<Availability> getByTutorAndDate(Long tutorId, LocalDate date) {
        return availabilityRepository.findByTutorIdAndSpecificDate(tutorId, date);
    }

    public List<Availability> getAvailableSlots(Long tutorId, DayOfWeek day,
                                                LocalTime start, LocalTime end) {
        return availabilityRepository.findAvailableSlots(tutorId, day, start, end);
    }

    // ── UPDATE ───────────────────────────────────────
    @Transactional
    public Availability updateAvailability(Long id, Availability updated) {
        Availability existing = getById(id);
        existing.setDayOfWeek(updated.getDayOfWeek());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setAvailable(updated.isAvailable());
        existing.setAvailabilityType(updated.getAvailabilityType());
        existing.setSpecificDate(updated.getSpecificDate());
        return availabilityRepository.save(existing);
    }

    @Transactional
    public Availability toggleAvailability(Long id) {
        Availability availability = getById(id);
        availability.setAvailable(!availability.isAvailable());
        return availabilityRepository.save(availability);
    }

    // ── DELETE ───────────────────────────────────────
    public void deleteAvailability(Long id) {
        availabilityRepository.deleteById(id);
    }

    public void deleteAllByTutor(Long tutorId) {
        availabilityRepository.deleteByTutorId(tutorId);
    }
}