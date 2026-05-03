package com.example.demo.QuizMS;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // ─── GET ALL ───────────────────────────────────────
    @GetMapping
    public List<Quiz> getAllQuizzes() {
        return quizService.findAll();
    }

    // ─── GET BY ID ─────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return quizService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── GET BY TEACHER ────────────────────────────────
    @GetMapping("/teacher/{teacherId}")
    public List<Quiz> getQuizzesByTeacher(@PathVariable Long teacherId) {
        return quizService.findByCreatedBy(teacherId);
    }

    // ✅ AJOUTÉ — alias /tutor/{id} utilisé par Angular
    @GetMapping("/tutor/{tutorId}")
    public List<Quiz> getQuizzesByTutor(@PathVariable Long tutorId) {
        return quizService.findByCreatedBy(tutorId);
    }

    // ─── GET BY LEVEL ──────────────────────────────────
    @GetMapping("/level/{level}")
    public List<Quiz> getQuizzesByLevel(@PathVariable Level level) {
        return quizService.findByLevel(level);
    }

    // ─── GET BY STATUS ─────────────────────────────────
    @GetMapping("/status/{status}")
    public List<Quiz> getQuizzesByStatus(@PathVariable QuizStatus status) {
        return quizService.findByStatus(status);
    }

    // ─── CREATE ────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Quiz> createQuiz(@Valid @RequestBody Quiz quiz) {
        Quiz saved = quizService.save(quiz);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ─── UPDATE ────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @Valid @RequestBody Quiz quiz) {
        try {
            Quiz updated = quizService.update(id, quiz);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── PUBLISH ───────────────────────────────────────
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Quiz> publishQuiz(@PathVariable Long id) {
        return quizService.findById(id)
                .map(quiz -> {
                    quiz.setStatus(QuizStatus.PUBLISHED);
                    quiz.setPublishedAt(LocalDateTime.now());
                    return ResponseEntity.ok(quizService.save(quiz));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── ARCHIVE ───────────────────────────────────────
    @PatchMapping("/{id}/archive")
    public ResponseEntity<Quiz> archiveQuiz(@PathVariable Long id) {
        return quizService.findById(id)
                .map(quiz -> {
                    quiz.setStatus(QuizStatus.ARCHIVED);
                    return ResponseEntity.ok(quizService.save(quiz));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ AJOUTÉ — remettre en DRAFT
    @PatchMapping("/{id}/draft")
    public ResponseEntity<Quiz> setDraft(@PathVariable Long id) {
        return quizService.findById(id)
                .map(quiz -> {
                    quiz.setStatus(QuizStatus.DRAFT);
                    quiz.setPublishedAt(null);
                    return ResponseEntity.ok(quizService.save(quiz));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── DELETE ────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        if (quizService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        quizService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}