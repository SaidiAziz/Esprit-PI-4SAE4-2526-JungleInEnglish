package com.example.demo.QuestionMS;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);

    List<Question> findByQuizId(Long quizId);

    long countByQuizId(Long quizId);
}