package com.example.demo.AnswerMS;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    public AnswerService(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    @Transactional(readOnly = true)
    public List<Answer> findAll() {
        return answerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Answer> findById(Long id) {
        return answerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Answer> findByQuestionId(Long questionId) {
        return answerRepository.findByQuestionIdOrderByOrderIndexAsc(questionId);
    }

    @Transactional
    public Answer save(Answer answer) {
        return answerRepository.save(answer);
    }

    @Transactional
    public Answer update(Long id, Answer newAnswer) {
        return answerRepository.findById(id)
                .map(a -> {
                    // Ne pas toucher à la question si elle n'est pas fournie
                    if (newAnswer.getQuestion() != null) {
                        a.setQuestion(newAnswer.getQuestion());
                    }
                    a.setText(newAnswer.getText());
                    a.setIsCorrect(newAnswer.getIsCorrect());
                    a.setOrderIndex(newAnswer.getOrderIndex());
                    return answerRepository.save(a);
                })
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
    }
    @Transactional
    public void deleteById(Long id) {
        answerRepository.deleteById(id);
    }
}