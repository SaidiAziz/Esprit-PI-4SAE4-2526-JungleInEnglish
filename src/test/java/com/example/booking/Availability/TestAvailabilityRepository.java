package com.example.booking.Availability;

import com.example.booking.AvailabilityMS.Availability;
import com.example.booking.AvailabilityMS.AvailabilityType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TestAvailabilityRepository {
    Availability save(Availability a);
    Optional<Availability> findById(Long id);
    List<Availability> findAll();
    void deleteById(Long id);
    void deleteByTutorId(Long tutorId);
    List<Availability> findByTutorId(Long tutorId);
    List<Availability> findByTutorIdAndAvailableTrue(Long tutorId);
    List<Availability> findByTutorIdAndAvailableIsTrue(Long tutorId);
    List<Availability> findByTutorIdAndDayOfWeek(Long tutorId, DayOfWeek day);
    List<Availability> findByTutorIdAndAvailabilityType(Long tutorId, AvailabilityType type);
    List<Availability> findByTutorIdAndSpecificDate(Long tutorId, LocalDate date);
    List<Availability> findAvailableSlots(Long tutorId, DayOfWeek day, LocalTime start, LocalTime end);
}