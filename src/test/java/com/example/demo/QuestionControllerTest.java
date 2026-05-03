package com.example.demo;

import com.example.demo.QuestionMS.Question;
import com.example.demo.QuestionMS.QuestionController;
import com.example.demo.QuestionMS.QuestionService;
import com.example.demo.QuizMS.Quiz;
import com.example.demo.QuizMS.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuestionController questionController;

    private Question question;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        quiz = new Quiz();
        quiz.setTitle("Test Quiz");

        question = new Question();
        question.setText("What is Java?");
        question.setPoints(5);
        question.setOrderIndex(1);
        question.setQuiz(quiz);
    }

    // ── GET ALL ──────────────────────────────────

    @Test
    void getAllQuestions_shouldReturnList() {
        when(questionService.findAll()).thenReturn(List.of(question));

        List<Question> result = questionController.getAllQuestions();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(questionService).findAll();
    }

    @Test
    void getAllQuestions_shouldReturnEmptyList() {
        when(questionService.findAll()).thenReturn(List.of());

        List<Question> result = questionController.getAllQuestions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── GET BY ID ──────────────────────────────────

    @Test
    void getQuestionById_shouldReturnQuestion_whenFound() {
        when(questionService.findById(1L)).thenReturn(Optional.of(question));

        ResponseEntity<Question> response = questionController.getQuestionById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(question, response.getBody());
    }

    @Test
    void getQuestionById_shouldReturn404_whenNotFound() {
        when(questionService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Question> response = questionController.getQuestionById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── GET BY QUIZ ID ──────────────────────────────────

    @Test
    void getQuestionsByQuizId_shouldReturnList() {
        when(questionService.findByQuizId(1L)).thenReturn(List.of(question));

        List<Question> result = questionController.getQuestionsByQuizId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ── CREATE ──────────────────────────────────

    @Test
    void createQuestion_shouldReturn400_whenQuizIdMissing() {
        Map<String, Object> body = new HashMap<>();
        body.put("text", "What is Java?");

        ResponseEntity<Object> response = questionController.createQuestion(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("quizId is required", response.getBody());
    }

    @Test
    void createQuestion_shouldReturn400_whenQuizNotFound() {
        Map<String, Object> body = new HashMap<>();
        body.put("quizId", "99");
        body.put("text", "What is Java?");

        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = questionController.createQuestion(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Quiz not found"));
    }

    @Test
    void createQuestion_shouldReturn201_whenValid() {
        Map<String, Object> body = new HashMap<>();
        body.put("quizId", "1");
        body.put("text", "What is Java?");
        body.put("points", "5");
        body.put("orderIndex", "1");

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(questionService.save(any(Question.class))).thenReturn(question);

        ResponseEntity<Object> response = questionController.createQuestion(body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void createQuestion_shouldReturn201_withAnswers() {
        Map<String, Object> answer1 = new HashMap<>();
        answer1.put("text", "A programming language");
        answer1.put("isCorrect", true);
        answer1.put("orderIndex", "0");

        Map<String, Object> body = new HashMap<>();
        body.put("quizId", "1");
        body.put("text", "What is Java?");
        body.put("points", "5");
        body.put("orderIndex", "1");
        body.put("answers", List.of(answer1));

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(questionService.save(any(Question.class))).thenReturn(question);

        ResponseEntity<Object> response = questionController.createQuestion(body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    // ── UPDATE ──────────────────────────────────

    @Test
    void updateQuestion_shouldReturn404_whenNotFound() {
        when(questionService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = questionController.updateQuestion(99L, new HashMap<>());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateQuestion_shouldReturn200_whenValid() {
        Map<String, Object> body = new HashMap<>();
        body.put("text", "Updated text");
        body.put("points", "10");
        body.put("orderIndex", "2");

        when(questionService.findById(1L)).thenReturn(Optional.of(question));
        when(questionService.save(any(Question.class))).thenReturn(question);

        ResponseEntity<Object> response = questionController.updateQuestion(1L, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(questionService).save(question);
    }

    @Test
    void updateQuestion_shouldUpdateQuiz_whenQuizIdProvided() {
        Map<String, Object> body = new HashMap<>();
        body.put("quizId", "2");

        Quiz newQuiz = new Quiz();
        when(questionService.findById(1L)).thenReturn(Optional.of(question));
        when(quizRepository.findById(2L)).thenReturn(Optional.of(newQuiz));
        when(questionService.save(any(Question.class))).thenReturn(question);

        ResponseEntity<Object> response = questionController.updateQuestion(1L, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(quizRepository).findById(2L);
    }

    // ── DELETE ──────────────────────────────────

    @Test
    void deleteQuestion_shouldReturn404_whenNotFound() {
        when(questionService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = questionController.deleteQuestion(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteQuestion_shouldReturn204_whenFound() {
        when(questionService.findById(1L)).thenReturn(Optional.of(question));
        doNothing().when(questionService).deleteById(1L);

        ResponseEntity<Void> response = questionController.deleteQuestion(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(questionService).deleteById(1L);
    }
}
