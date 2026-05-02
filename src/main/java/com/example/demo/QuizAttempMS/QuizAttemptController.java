package com.example.demo.QuizAttempMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz-attempts")
@CrossOrigin(origins = "*")
public class QuizAttemptController {

    @Autowired
    private QuizAttemptService attemptService;

    // POST /api/quiz-attempts/start
    @PostMapping("/start")
    public ResponseEntity<QuizAttempt> startQuiz(
            @RequestParam Long quizId,
            @RequestParam Long studentId,
            @RequestParam(required = false) Long bookingId) {
        return ResponseEntity.ok(attemptService.startQuiz(quizId, studentId, bookingId));
    }

    // POST /api/quiz-attempts/{attemptId}/answer
    @PostMapping("/{attemptId}/answer")
    public ResponseEntity<Map<String, Object>> submitAnswer(
            @PathVariable Long attemptId,
            @RequestParam Long questionId,
            @RequestParam Long selectedAnswerId,
            @RequestParam(required = false) Integer responseTimeSec) {
        return ResponseEntity.ok(
                attemptService.submitAnswer(attemptId, questionId, selectedAnswerId, responseTimeSec)
        );
    }

    // PATCH /api/quiz-attempts/{attemptId}/complete
    @PatchMapping("/{attemptId}/complete")
    public ResponseEntity<QuizAttempt> completeQuiz(@PathVariable Long attemptId) {
        return ResponseEntity.ok(attemptService.completeQuiz(attemptId));
    }

    // PATCH /api/quiz-attempts/{attemptId}/timeout
    @PatchMapping("/{attemptId}/timeout")
    public ResponseEntity<QuizAttempt> timeoutQuiz(@PathVariable Long attemptId) {
        return ResponseEntity.ok(attemptService.timeoutQuiz(attemptId));
    }

    // GET /api/quiz-attempts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<QuizAttempt> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.getAttemptById(id));
    }

    // GET /api/quiz-attempts/student/{studentId}
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<QuizAttempt>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attemptService.getByStudent(studentId));
    }

    // GET /api/quiz-attempts/student/{studentId}/completed
    @GetMapping("/student/{studentId}/completed")
    public ResponseEntity<List<QuizAttempt>> getCompletedByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attemptService.getCompletedByStudent(studentId));
    }

    // ✅ AJOUTÉ — GET /api/quiz-attempts/student/{studentId}/quiz/{quizId}
    @GetMapping("/student/{studentId}/quiz/{quizId}")
    public ResponseEntity<List<QuizAttempt>> getByStudentAndQuiz(
            @PathVariable Long studentId,
            @PathVariable Long quizId) {
        return ResponseEntity.ok(attemptService.getByStudentAndQuiz(studentId, quizId));
    }

    // GET /api/quiz-attempts/quiz/{quizId}
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizAttempt>> getByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(attemptService.getByQuiz(quizId));
    }

    // GET /api/quiz-attempts/student/{studentId}/stats
    @GetMapping("/student/{studentId}/stats")
    public ResponseEntity<Map<String, Object>> getStudentStats(@PathVariable Long studentId) {
        return ResponseEntity.ok(attemptService.getStudentStats(studentId));
    }

    // GET /api/quiz-attempts/leaderboard
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Object[]>> getLeaderboard() {
        return ResponseEntity.ok(attemptService.getMonthlyLeaderboard());
    }
    // ─── AJOUTER ces endpoints dans QuizAttemptController.java ───────────────────

    // ✅ 1. Stats détaillées par quiz (pour tutor)
// GET /api/quiz-attempts/quiz/{quizId}/stats
    @GetMapping("/quiz/{quizId}/stats")
    public ResponseEntity<Map<String, Object>> getQuizStats(@PathVariable Long quizId) {
        return ResponseEntity.ok(attemptService.getDetailedQuizStats(quizId));
    }

    // ✅ 2. Progression du student dans le temps (pour graphique)
// GET /api/quiz-attempts/student/{studentId}/progression
    @GetMapping("/student/{studentId}/progression")
    public ResponseEntity<List<Map<String, Object>>> getStudentProgression(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(attemptService.getStudentProgression(studentId));
    }

    // ✅ 3. Badges du student
// GET /api/quiz-attempts/student/{studentId}/badges
    @GetMapping("/student/{studentId}/badges")
    public ResponseEntity<List<Map<String, Object>>> getStudentBadges(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(attemptService.getStudentBadges(studentId));
    }
}