package com.example.demo.AnswerMS;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdOrderByOrderIndexAsc(Long questionId);

    long countByQuestionId(Long questionId);
}