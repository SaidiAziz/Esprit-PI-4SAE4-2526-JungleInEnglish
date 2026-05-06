package com.example.demo.QuizAttempMS;

import com.example.demo.AnswerMS.AttemptStatus;
import com.example.demo.StudentAnswerMS.StudentAnswer;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempt")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;


    // ── Score ──────────────────────────────────────
    @Column(nullable = false)
    private Integer score = 0;          // bonnes réponses

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;    // total points possibles

    @Column(nullable = false)
    private Double percentage = 0.0;    // score en %

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned = 0;       // XP gagné

    // ── Vies ───────────────────────────────────────
    @Column(name = "lives_remaining", nullable = false)
    private Integer livesRemaining = 3;

    // ── Timing ─────────────────────────────────────
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // ── Status ─────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed = false;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;

    // ── Relation ───────────────────────────────────
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentAnswer> studentAnswers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }

    public QuizAttempt() {}

    // ── Getters & Setters ──────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }



    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public Integer getXpEarned() { return xpEarned; }
    public void setXpEarned(Integer xpEarned) { this.xpEarned = xpEarned; }

    public Integer getLivesRemaining() { return livesRemaining; }
    public void setLivesRemaining(Integer livesRemaining) { this.livesRemaining = livesRemaining; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public Boolean getIsPassed() { return isPassed; }
    public void setIsPassed(Boolean isPassed) { this.isPassed = isPassed; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public List<StudentAnswer> getStudentAnswers() { return studentAnswers; }
    public void setStudentAnswers(List<StudentAnswer> studentAnswers) { this.studentAnswers = studentAnswers; }
}