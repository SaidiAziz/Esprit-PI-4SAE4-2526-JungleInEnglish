package tn.esprit.administrationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.administrationservice.dto.TutorSessionStatsDTO;
import tn.esprit.administrationservice.entity.Session;
import tn.esprit.administrationservice.entity.SessionStatus;
import tn.esprit.administrationservice.dto.MonthlySessionStatsDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

  List<Session> findByTutorId(Long tutorId);
  List<Session> findByCourseId(Long courseId);
  List<Session> findBySessionDate(LocalDate sessionDate);

  // ✅ CREATE: détecte si le tuteur a déjà une session qui chevauche (même date)
  @Query("""
        select count(s) > 0
        from Session s
        where s.tutorId = :tutorId
          and s.sessionDate = :sessionDate
          and s.status <> :cancelled
          and (:startTime < s.endTime and :endTime > s.startTime)
    """)
  boolean existsConflictForTutor(
    @Param("tutorId") Long tutorId,
    @Param("sessionDate") LocalDate sessionDate,
    @Param("startTime") LocalTime startTime,
    @Param("endTime") LocalTime endTime,
    @Param("cancelled") SessionStatus cancelled
  );

  // ✅ UPDATE: pareil mais on exclut la session qu’on est en train de modifier
  @Query("""
        select count(s) > 0
        from Session s
        where s.tutorId = :tutorId
          and s.sessionDate = :sessionDate
          and s.status <> :cancelled
          and s.id <> :sessionId
          and (:startTime < s.endTime and :endTime > s.startTime)
    """)
  boolean existsConflictForTutorExcludingSession(
    @Param("sessionId") Long sessionId,
    @Param("tutorId") Long tutorId,
    @Param("sessionDate") LocalDate sessionDate,
    @Param("startTime") LocalTime startTime,
    @Param("endTime") LocalTime endTime,
    @Param("cancelled") SessionStatus cancelled
  );

  // =========================
  // STATISTIQUES
  // =========================

  @Query("select count(s) from Session s")
  long countAllSessions();

  @Query("select count(s) from Session s where s.status = :status")
  long countByStatus(@Param("status") SessionStatus status);

  @Query("""
        select new tn.esprit.administrationservice.dto.TutorSessionStatsDTO(s.tutorId, count(s))
        from Session s
        group by s.tutorId
        order by count(s) desc
    """)
  List<TutorSessionStatsDTO> countSessionsByTutor();

  @Query("""
    select new tn.esprit.administrationservice.dto.MonthlySessionStatsDTO(month(s.sessionDate), count(s))
    from Session s
    group by month(s.sessionDate)
    order by month(s.sessionDate)
""")
  List<MonthlySessionStatsDTO> countSessionsByMonth();
}
