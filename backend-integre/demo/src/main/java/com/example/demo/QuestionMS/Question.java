package com.example.demo.QuestionMS;

import com.example.demo.AnswerMS.Answer;
import com.example.demo.QuizMS.Quiz;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    @JsonIgnore
    private Quiz quiz;

    @NotBlank(message = "Question text is required")
    @Size(min = 5, max = 500, message = "Question must be between 5 and 500 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index cannot be negative")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @NotNull(message = "Points is required")
    @Min(value = 1, message = "Question must be worth at least 1 point")
    @Max(value = 100, message = "Question cannot be worth more than 100 points")
    @Column(nullable = false)
    private Integer points = 1;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Answer> answers = new ArrayList<>();

    public Question() {}

    public Question(Quiz quiz, String text, Integer orderIndex, Integer points, List<Answer> answers) {
        this.quiz = quiz;
        this.text = text;
        this.orderIndex = orderIndex;
        this.points = points;
        this.answers = answers;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public List<Answer> getAnswers() { return answers; }
    public void setAnswers(List<Answer> answers) { this.answers = answers; }
}