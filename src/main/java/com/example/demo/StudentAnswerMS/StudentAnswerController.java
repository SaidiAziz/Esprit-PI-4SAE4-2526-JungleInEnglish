package com.example.demo.StudentAnswerMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/student-answers")
@CrossOrigin(origins = "*")
public class StudentAnswerController {

    @Autowired
    private StudentAnswerService studentAnswerService;

    // GET /api/student-answers/attempt/{attemptId}
    // → Toutes les réponses d'une tentative
    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<List<StudentAnswer>> getByAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(studentAnswerService.getByAttempt(attemptId));
    }

    // GET /api/student-answers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<StudentAnswer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentAnswerService.getById(id));
    }

    // GET /api/student-answers/student/{studentId}/question/{questionId}
    // → Historique des réponses d'un étudiant sur une question
    @GetMapping("/student/{studentId}/question/{questionId}")
    public ResponseEntity<List<StudentAnswer>> getByStudentAndQuestion(
            @PathVariable Long studentId,
            @PathVariable Long questionId) {
        return ResponseEntity.ok(
                studentAnswerService.getByStudentAndQuestion(studentId, questionId)
        );
    }
}