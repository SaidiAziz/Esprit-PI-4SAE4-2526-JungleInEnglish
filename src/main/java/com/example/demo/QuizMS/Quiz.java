package com.example.demo.QuizMS;

import com.example.demo.QuestionMS.Question;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Level is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 180, message = "Duration cannot exceed 180 minutes")
    @Column(nullable = false)
    private Integer duration;

    @NotNull(message = "Passing score is required")
    @Min(value = 0, message = "Passing score cannot be negative")
    @Max(value = 100, message = "Passing score cannot exceed 100")
    @Column(name = "passing_score", nullable = false)
    private Integer passingScore;

    @Min(value = 1, message = "Max attempts must be at least 1")
    @Max(value = 10, message = "Max attempts cannot exceed 10")
    @Column(name = "max_attempts")
    private Integer maxAttempts = 1;

    @Column(name = "is_adaptive", nullable = false)
    private Boolean isAdaptive = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status = QuizStatus.DRAFT;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @NotNull(message = "Creator ID is required")
    @Column(name = "created_by")
    private Long createdBy;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<Question> questions;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Quiz() {}

    public Quiz(String title, Level level,
                Integer duration, Integer passingScore, Boolean isAdaptive) {
        this.title = title;
        this.level = level;
        this.duration = duration;
        this.passingScore = passingScore;
        this.isAdaptive = isAdaptive != null ? isAdaptive : false;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }
    public Boolean getIsAdaptive() { return isAdaptive; }
    public void setIsAdaptive(Boolean adaptive) { isAdaptive = adaptive; }
    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Boolean getAdaptive() { return isAdaptive; }
    public void setAdaptive(Boolean adaptive) { isAdaptive = adaptive; }
    public QuizStatus getStatus() { return status; }
    public void setStatus(QuizStatus status) { this.status = status; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}