package com.example.demo.QuizMS;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public List<Quiz> findAll() {
        return quizRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Quiz> findByLevel(Level level) {
        return quizRepository.findByLevel(level);
    }


    @Transactional
    public Quiz save(Quiz quiz) {
        return quizRepository.save(quiz);
    }
    @Transactional
    public Quiz update(Long id, Quiz quiz) {
        return quizRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(quiz.getTitle());
                    existing.setLevel(quiz.getLevel());
                    existing.setDuration(quiz.getDuration());
                    existing.setPassingScore(quiz.getPassingScore());
                    existing.setIsAdaptive(quiz.getIsAdaptive());
                    existing.setMaxAttempts(quiz.getMaxAttempts());
                    if (quiz.getStatus() != null) {
                        existing.setStatus(quiz.getStatus());
                    }
                    return quizRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: id=" + id));
    }

@Transactional
public List<Quiz> findByStatus(QuizStatus status){
        return quizRepository.findByStatus(status);
}

    @Transactional(readOnly = true)
    public List<Quiz> findByCreatedBy(Long teacherId) {
        return quizRepository.findByCreatedBy(teacherId);
    }

    @Transactional
    public void deleteById(Long id) {
        quizRepository.deleteById(id);
    }
}
