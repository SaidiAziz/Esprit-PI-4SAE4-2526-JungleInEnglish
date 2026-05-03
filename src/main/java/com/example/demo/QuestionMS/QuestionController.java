package com.example.demo.QuestionMS;

import com.example.demo.QuizMS.Quiz;
import com.example.demo.QuizMS.QuizRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuizRepository quizRepository;   // ✅ ajouté

    public QuestionController(QuestionService questionService,
                              QuizRepository quizRepository) {
        this.questionService = questionService;
        this.quizRepository  = quizRepository;
    }

    // GET /api/questions
    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.findAll();
    }

    // GET /api/questions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        return questionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/questions/quiz/{quizId}
    @GetMapping("/quiz/{quizId}")
    public List<Question> getQuestionsByQuizId(@PathVariable Long quizId) {
        return questionService.findByQuizId(quizId);
    }

    // POST /api/questions
    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody Map<String, Object> body) {

        // ✅ FIX — lire quizId depuis le JSON et charger le Quiz
        Object quizIdObj = body.get("quizId");
        if (quizIdObj == null) {
            return ResponseEntity.badRequest().body("quizId is required");
        }

        Long quizId = Long.valueOf(quizIdObj.toString());
        Quiz quiz   = quizRepository.findById(quizId)
                .orElse(null);

        if (quiz == null) {
            return ResponseEntity.badRequest().body("Quiz not found: " + quizId);
        }

        // ✅ Construire la Question manuellement depuis le body
        Question question = new Question();
        question.setQuiz(quiz);
        question.setText((String) body.get("text"));
        question.setOrderIndex(body.get("orderIndex") != null
                ? Integer.valueOf(body.get("orderIndex").toString()) : 0);
        question.setPoints(body.get("points") != null
                ? Integer.valueOf(body.get("points").toString()) : 1);

        // ✅ Mapper les answers si présentes
        if (body.get("answers") instanceof List<?> rawAnswers) {
            for (Object rawAnswer : rawAnswers) {
                if (rawAnswer instanceof Map<?, ?> answerMap) {
                    com.example.demo.AnswerMS.Answer answer = new com.example.demo.AnswerMS.Answer();
                    answer.setText((String) answerMap.get("text"));
                    answer.setIsCorrect(Boolean.TRUE.equals(answerMap.get("isCorrect")));
                    answer.setOrderIndex(answerMap.get("orderIndex") != null
                            ? Integer.valueOf(answerMap.get("orderIndex").toString()) : 0);
                    answer.setQuestion(question);  // ✅ lien bidirectionnel
                    question.getAnswers().add(answer);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.save(question));
    }

    // PUT /api/questions/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id,
                                            @RequestBody Map<String, Object> body) {
        Question existing = questionService.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();

        // Mettre à jour le quiz si quizId fourni
        if (body.get("quizId") != null) {
            Long quizId = Long.valueOf(body.get("quizId").toString());
            quizRepository.findById(quizId).ifPresent(existing::setQuiz);
        }

        if (body.get("text")       != null) existing.setText((String) body.get("text"));
        if (body.get("orderIndex") != null) existing.setOrderIndex(Integer.valueOf(body.get("orderIndex").toString()));
        if (body.get("points")     != null) existing.setPoints(Integer.valueOf(body.get("points").toString()));

        return ResponseEntity.ok(questionService.save(existing));
    }

    // DELETE /api/questions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        if (questionService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        questionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}