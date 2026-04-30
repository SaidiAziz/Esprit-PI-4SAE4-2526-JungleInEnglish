package com.example.demo.QuizAttempMS;

import com.example.demo.AnswerMS.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


    @Repository
    public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

        // Toutes les tentatives d'un étudiant
        List<QuizAttempt> findByStudentId(Long studentId);

        // Toutes les tentatives sur un quiz
        List<QuizAttempt> findByQuizId(Long quizId);

        // Tentatives d'un étudiant sur un quiz spécifique
        List<QuizAttempt> findByStudentIdAndQuizId(Long studentId, Long quizId);

        // Tentative en cours d'un étudiant sur un quiz
        Optional<QuizAttempt> findByStudentIdAndQuizIdAndStatus(
                Long studentId, Long quizId, AttemptStatus status
        );

        // Nombre de tentatives d'un étudiant sur un quiz
        long countByStudentIdAndQuizId(Long studentId, Long quizId);

        // Toutes les tentatives complétées d'un étudiant
        List<QuizAttempt> findByStudentIdAndStatus(Long studentId, AttemptStatus status);

        // Meilleur score d'un étudiant sur un quiz
        @Query("SELECT MAX(a.percentage) FROM QuizAttempt a " +
                "WHERE a.studentId = :studentId AND a.quizId = :quizId " +
                "AND a.status = 'COMPLETED'")
        Double findBestScore(@Param("studentId") Long studentId,
                             @Param("quizId") Long quizId);

        // Total XP d'un étudiant
        @Query("SELECT COALESCE(SUM(a.xpEarned), 0) FROM QuizAttempt a " +
                "WHERE a.studentId = :studentId AND a.status = 'COMPLETED'")
        Integer findTotalXp(@Param("studentId") Long studentId);

        // Nombre de quiz complétés
        @Query("SELECT COUNT(a) FROM QuizAttempt a " +
                "WHERE a.studentId = :studentId AND a.status = 'COMPLETED'")
        long countCompleted(@Param("studentId") Long studentId);

        // Nombre de scores parfaits (100%)
        @Query("SELECT COUNT(a) FROM QuizAttempt a " +
                "WHERE a.studentId = :studentId AND a.percentage = 100.0 " +
                "AND a.status = 'COMPLETED'")
        long countPerfectScores(@Param("studentId") Long studentId);

        // Streak — jours consécutifs d'activité
        @Query(value = "SELECT COUNT(DISTINCT DATE(completed_at)) FROM quiz_attempt " +
                "WHERE student_id = :studentId AND status = 'COMPLETED' " +
                "AND completed_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)",
                nativeQuery = true)
        int countActiveDaysLast30(@Param("studentId") Long studentId);

        // Leaderboard mensuel
        @Query("SELECT a.studentId, SUM(a.xpEarned) as totalXp " +
                "FROM QuizAttempt a " +
                "WHERE a.status = 'COMPLETED' " +
                "AND MONTH(a.completedAt) = MONTH(CURRENT_DATE) " +
                "GROUP BY a.studentId " +
                "ORDER BY totalXp DESC")
        List<Object[]> findMonthlyLeaderboard();
    }


