package com.example.booking.BookingMS;

import com.example.booking.BookingMS.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStudentId(Long studentId);

    List<Booking> findByTutorId(Long tutorId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByType(BookingType type);

    List<Booking> findByStudentIdAndStatus(Long studentId, BookingStatus status);

    List<Booking> findByTutorIdAndStatus(Long tutorId, BookingStatus status);
    List<Booking> findByStatusAndCreatedAtBefore(
            BookingStatus status,
            LocalDateTime dateTime
    );
    List<Booking> findBySessionDate(LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.tutorId = :tutorId " +
            "AND b.sessionDate = :date " +
            "AND b.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    List<Booking> findConflictingBookings(
            @Param("tutorId") Long tutorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tutorId = :tutorId AND b.status = 'COMPLETED'")
    Long countCompletedSessionsByTutor(@Param("tutorId") Long tutorId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.studentId = :studentId AND b.status = 'COMPLETED'")
    Long countCompletedSessionsByStudent(@Param("studentId") Long studentId);
}

