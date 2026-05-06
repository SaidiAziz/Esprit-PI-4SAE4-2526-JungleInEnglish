package com.example.booking.SessionMS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByBookingId(Long bookingId);

    List<Session> findByStatus(SessionStatus status);

    @Query("SELECT s FROM Session s WHERE s.booking.tutorId = :tutorId")
    List<Session> findByTutorId(@Param("tutorId") Long tutorId);

    @Query("SELECT s FROM Session s WHERE s.booking.studentId = :studentId")
    List<Session> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT s FROM Session s WHERE s.booking.tutorId = :tutorId AND s.status = :status")
    List<Session> findByTutorIdAndStatus(
            @Param("tutorId") Long tutorId,
            @Param("status") SessionStatus status
    );

    @Query("SELECT AVG(sf.rating) FROM SessionFeedback sf WHERE sf.session.booking.tutorId = :tutorId")
    Double getAverageRatingByTutor(@Param("tutorId") Long tutorId);
    // ── Dashboard queries ─────────────────────────────────

    @Query("SELECT COUNT(s) FROM Session s WHERE s.booking.tutorId = :tutorId")
    long countByTutorId(@Param("tutorId") Long tutorId);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.booking.studentId = :studentId")
    long countByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.booking.tutorId = :tutorId AND s.status = :status")
    long countByTutorIdAndStatus(@Param("tutorId") Long tutorId, @Param("status") SessionStatus status);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.booking.studentId = :studentId AND s.status = :status")
    long countByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") SessionStatus status);

    @Query("SELECT s FROM Session s WHERE s.booking.studentId = :studentId AND s.status = :status")
    List<Session> findByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") SessionStatus status);

    @Query("SELECT COALESCE(SUM(s.duration), 0) FROM Session s WHERE s.booking.studentId = :studentId AND s.status = 'DONE'")
    Integer getTotalMinutesByStudent(@Param("studentId") Long studentId);
}
