package com.example.demo.QuestionMS;

import com.example.demo.QuizMS.Quiz;
import com.example.demo.QuizMS.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    // ── Constants ──────────────────────────────────
    private static final String QUIZ_ID = "quizId";
    private static final String ORDER_INDEX = "orderIndex";
    private static final String POINTS = "points";
    private static final String TEXT = "text";
    private static final String IS_CORRECT = "isCorrect";
    private static final String ANSWERS = "answers";

    private final QuestionService questionService;
    private final QuizRepository quizRepository;

    public QuestionController(QuestionService questionService,
                              QuizRepository quizRepository) {
        this.questionService = questionService;
        this.quizRepository  = quizRepository;
    }

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(@PathVariable Long id) {
        return questionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quiz/{quizId}")
    public List<Question> getQuestionsByQuizId(@PathVariable Long quizId) {
        return questionService.findByQuizId(quizId);
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public ResponseEntity<Object> createQuestion(@RequestBody Map<String, Object> body) {

        Object quizIdObj = body.get(QUIZ_ID);
        if (quizIdObj == null) {
            return ResponseEntity.badRequest().body("quizId is required");
        }

        Long quizId = Long.valueOf(quizIdObj.toString());
        Quiz quiz = quizRepository.findById(quizId).orElse(null);

        if (quiz == null) {
            return ResponseEntity.badRequest().body("Quiz not found: " + quizId);
        }

        Question question = new Question();
        question.setQuiz(quiz);
        question.setText((String) body.get(TEXT));
        question.setOrderIndex(body.get(ORDER_INDEX) != null
                ? Integer.valueOf(body.get(ORDER_INDEX).toString()) : 0);
        question.setPoints(body.get(POINTS) != null
                ? Integer.valueOf(body.get(POINTS).toString()) : 1);

        Object answersObj = body.get(ANSWERS);
        if (answersObj instanceof List<?> rawAnswers) {
            for (Object rawAnswer : rawAnswers) {
                if (rawAnswer instanceof Map) {
                    Map<String, Object> answerMap = (Map<String, Object>) rawAnswer;
                    com.example.demo.AnswerMS.Answer answer =
                            new com.example.demo.AnswerMS.Answer();
                    answer.setText((String) answerMap.get(TEXT));
                    answer.setIsCorrect(Boolean.TRUE.equals(answerMap.get(IS_CORRECT)));
                    answer.setOrderIndex(answerMap.get(ORDER_INDEX) != null
                            ? Integer.valueOf(answerMap.get(ORDER_INDEX).toString()) : 0);
                    answer.setQuestion(question);
                    question.getAnswers().add(answer);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.save(question));
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateQuestion(@PathVariable Long id,
                                                 @RequestBody Map<String, Object> body) {
        Question existing = questionService.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();

        if (body.get(QUIZ_ID) != null) {
            Long quizId = Long.valueOf(body.get(QUIZ_ID).toString());
            quizRepository.findById(quizId).ifPresent(existing::setQuiz);
        }

        if (body.get(TEXT) != null) existing.setText((String) body.get(TEXT));
        if (body.get(ORDER_INDEX) != null)
            existing.setOrderIndex(Integer.valueOf(body.get(ORDER_INDEX).toString()));
        if (body.get(POINTS) != null)
            existing.setPoints(Integer.valueOf(body.get(POINTS).toString()));

        return ResponseEntity.ok(questionService.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        if (questionService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        questionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}