package com.example.demo.AnswerMS;

import com.example.demo.QuestionMS.Question;
import com.example.demo.QuestionMS.QuestionRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    private final AnswerService answerService;
    private final QuestionRepository questionRepository;

    public AnswerController(AnswerService answerService, QuestionRepository questionRepository) {
        this.answerService = answerService;
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public List<Answer> getAllAnswers() {
        return answerService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Answer> getAnswerById(@PathVariable Long id) {
        return answerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/question/{questionId}")
    public List<Answer> getAnswersByQuestionId(@PathVariable Long questionId) {
        return answerService.findByQuestionId(questionId);
    }

    @PostMapping
    public ResponseEntity<Answer> createAnswer(@Valid @RequestBody Answer answer) {
        Question question = questionRepository
                .findById(answer.getQuestion().getId())
                .orElseThrow(() -> new RuntimeException("Question not found"));
        answer.setQuestion(question);
        return ResponseEntity.ok(answerService.save(answer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Answer> updateAnswer(@PathVariable Long id, @Valid @RequestBody Answer answer) {
        try {
            // ✅ Fix: vérifier que question n'est pas null avant d'accéder à getId()
            if (answer.getQuestion() != null && answer.getQuestion().getId() != null) {
                Question question = questionRepository.findById(answer.getQuestion().getId())
                        .orElseThrow(() -> new RuntimeException("Question not found"));
                answer.setQuestion(question);
            }
            return ResponseEntity.ok(answerService.update(id, answer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        if (answerService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        answerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}