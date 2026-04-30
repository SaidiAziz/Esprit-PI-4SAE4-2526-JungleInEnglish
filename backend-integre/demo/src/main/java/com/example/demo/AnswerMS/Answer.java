package com.example.demo.AnswerMS;

import com.example.demo.QuestionMS.Question;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "answer")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @NotBlank(message = "Answer text is required")
    @Size(min = 1, max = 300, message = "Answer must be between 1 and 300 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @NotNull(message = "isCorrect field is required")
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index cannot be negative")
    @Max(value = 99, message = "Order index cannot exceed 99")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    public Answer() {}

    public Answer(Question question, String text, Boolean isCorrect, Integer orderIndex) {
        this.question = question;
        this.text = text;
        this.isCorrect = isCorrect != null ? isCorrect : false;
        this.orderIndex = orderIndex != null ? orderIndex : 0;
    }

    public Long getId() { return id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean correct) { this.isCorrect = correct; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}