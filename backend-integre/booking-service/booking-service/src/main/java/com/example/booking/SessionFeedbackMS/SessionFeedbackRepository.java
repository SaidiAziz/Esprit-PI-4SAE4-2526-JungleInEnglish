package com.example.booking.SessionFeedbackMS;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionFeedbackRepository extends JpaRepository<SessionFeedback, Long> {

    Optional<SessionFeedback> findBySessionId(Long sessionId);

    List<SessionFeedback> findByStudentId(Long studentId);

    @Query("SELECT sf FROM SessionFeedback sf WHERE sf.session.booking.tutorId = :tutorId")
    List<SessionFeedback> findByTutorId(@Param("tutorId") Long tutorId);

    @Query("SELECT AVG(sf.rating) FROM SessionFeedback sf WHERE sf.session.booking.tutorId = :tutorId")
    Double getAverageRatingByTutor(@Param("tutorId") Long tutorId);

    @Query("SELECT sf FROM SessionFeedback sf WHERE sf.rating >= :minRating")
    List<SessionFeedback> findByRatingGreaterThanEqual(@Param("minRating") Integer minRating);

    boolean existsBySessionId(Long sessionId);
}
