package com.example.demo.StudentAnswerMS;

import com.example.demo.QuizAttempMS.QuizAttempt;
import com.example.demo.QuizAttempMS.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentAnswerService {

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    // ── READ ─────────────────────────────────────
    public List<StudentAnswer> getByAttempt(Long attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));
        return studentAnswerRepository.findByAttempt(attempt);
    }

    public StudentAnswer getById(Long id) {
        return studentAnswerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudentAnswer not found: " + id));
    }

    // ── Stats par question ───────────────────────
    public List<StudentAnswer> getByStudentAndQuestion(Long studentId, Long questionId) {
        return studentAnswerRepository.findByStudentAndQuestion(studentId, questionId);
    }
}