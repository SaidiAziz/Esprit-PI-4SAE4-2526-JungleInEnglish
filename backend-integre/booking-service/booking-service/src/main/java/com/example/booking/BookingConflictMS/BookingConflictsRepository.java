package com.example.booking.BookingConflictMS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingConflictsRepository extends JpaRepository<BookingConflict, Long> {

    List<BookingConflict> findByTutorId(Long tutorId);

    List<BookingConflict> findByConflictDate(LocalDate date);

    List<BookingConflict> findByTutorIdAndConflictDate(Long tutorId, LocalDate date);

    @Query("SELECT bc FROM BookingConflict bc WHERE bc.tutorId = :tutorId ORDER BY bc.detectedAt DESC")
    List<BookingConflict> findRecentConflictsByTutor(@Param("tutorId") Long tutorId);

    void deleteByTutorId(Long tutorId);
}
