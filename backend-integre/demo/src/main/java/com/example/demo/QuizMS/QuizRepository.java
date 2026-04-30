package com.example.demo.QuizMS;

import com.example.demo.QuizMS.Level;
import com.example.demo.QuizMS.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {



  List<Quiz> findByLevel(Level level);
    List<Quiz> findByCreatedBy(Long createdBy);
    List<Quiz> findByStatus(QuizStatus status);
  List<Quiz> findByCreatedByAndStatus(Long teacherId, QuizStatus status);

  @Query("SELECT q FROM Quiz q WHERE q.status = 'PUBLISHED' ORDER BY q.createdAt DESC")
  List<Quiz> findAllPublished();
}