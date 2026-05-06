package com.example.booking.AvailabilityMS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
        List<Availability> findByTutorIdAndAvailableTrue(Long tutorId);

        List<Availability> findByTutorId(Long tutorId);

        List<Availability> findByTutorIdAndAvailableIsTrue(Long tutorId);

        List<Availability> findByTutorIdAndDayOfWeek(Long tutorId, DayOfWeek dayOfWeek);

        List<Availability> findByTutorIdAndAvailabilityType(Long tutorId, AvailabilityType type);

        List<Availability> findByTutorIdAndSpecificDate(Long tutorId, LocalDate specificDate);

        @Query("SELECT a FROM Availability a WHERE a.tutorId = :tutorId " +
                        "AND a.available = true " +
                        "AND a.dayOfWeek = :day " +
                        "AND a.startTime <= :start " +
                        "AND a.endTime >= :end")
        List<Availability> findAvailableSlots(
                        @Param("tutorId") Long tutorId,
                        @Param("day") DayOfWeek day,
                        @Param("start") LocalTime start,
                        @Param("end") LocalTime end);

        void deleteByTutorId(Long tutorId);
}
