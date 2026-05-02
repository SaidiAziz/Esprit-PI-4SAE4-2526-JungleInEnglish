package com.example.demo.StudentAnswerMS;

import com.example.demo.QuizAttempMS.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    // Toutes les réponses d'une tentative
    List<StudentAnswer> findByAttempt(QuizAttempt attempt);

    // Réponse d'une tentative pour une question spécifique
    Optional<StudentAnswer> findByAttemptAndQuestionId(
            QuizAttempt attempt, Long questionId
    );

    // Toutes les réponses d'un étudiant sur une question
    @Query("SELECT sa FROM StudentAnswer sa " +
            "JOIN sa.attempt a " +
            "WHERE a.studentId = :studentId AND sa.questionId = :questionId")
    List<StudentAnswer> findByStudentAndQuestion(
            @Param("studentId") Long studentId,
            @Param("questionId") Long questionId
    );
}