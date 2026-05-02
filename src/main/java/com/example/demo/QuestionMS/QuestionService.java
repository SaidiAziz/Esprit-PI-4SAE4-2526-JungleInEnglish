package com.example.demo.QuestionMS;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Question> findById(Long id) {
        return questionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Question> findByQuizId(Long quizId) {
        return questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
    }

    @Transactional
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Transactional
    public Question update(Long id, Question question) {
        return questionRepository.findById(id)
                .map(q -> {
                    q.setQuiz(question.getQuiz());
                    q.setText(question.getText());
                    q.setOrderIndex(question.getOrderIndex());
                    q.setPoints(question.getPoints());
                    return questionRepository.save(q);
                })
                .orElseThrow(() -> new IllegalArgumentException("Question non trouvée: id=" + id));
    }
    @Transactional
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }
}