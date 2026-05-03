package com.example.demo.QuizAttempMS;

import com.example.demo.AnswerMS.Answer;
import com.example.demo.AnswerMS.AttemptStatus;
import com.example.demo.QuestionMS.Question;
import com.example.demo.QuizMS.Quiz;
import com.example.demo.QuizMS.QuizRepository;
import com.example.demo.QuizMS.QuizStatus;
import com.example.demo.StudentAnswerMS.StudentAnswer;
import com.example.demo.StudentAnswerMS.StudentAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    // ── Constants ──────────────────────────────────
    private static final String IS_CORRECT = "isCorrect";
    private static final String LIVES_REMAINING = "livesRemaining";
    private static final String GAME_OVER = "gameOver";
    private static final String XP_EARNED = "xpEarned";
    private static final String POINTS_EARNED = "pointsEarned";
    private static final String PERCENTAGE = "percentage";
    private static final String CORRECT_ANSWER_ID = "correctAnswerId";

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Transactional
    public QuizAttempt startQuiz(Long quizId, Long studentId, Long bookingId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found: " + quizId));

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new RuntimeException("Quiz is not published yet");
        }

        Optional<QuizAttempt> existing = attemptRepository.findByStudentIdAndQuizIdAndStatus(
                studentId, quizId, AttemptStatus.IN_PROGRESS
        );
        if (existing.isPresent()) {
            return existing.get();
        }

        long previousAttempts = attemptRepository.countByStudentIdAndQuizId(studentId, quizId);
        if (quiz.getMaxAttempts() != null && previousAttempts >= quiz.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempts reached for this quiz");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setStudentId(studentId);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setLivesRemaining(3);
        attempt.setAttemptNumber((int) previousAttempts + 1);

        int totalPoints = quiz.getQuestions().stream()
                .mapToInt(Question::getPoints)
                .sum();
        attempt.setTotalPoints(totalPoints);

        return attemptRepository.save(attempt);
    }

    public List<QuizAttempt> getByStudentAndQuiz(Long studentId, Long quizId) {
        return attemptRepository.findByStudentIdAndQuizId(studentId, quizId);
    }

    @Transactional
    public Map<String, Object> submitAnswer(Long attemptId, Long questionId,
                                            Long selectedAnswerId, Integer responseTimeSec) {
        QuizAttempt attempt = getAttemptById(attemptId);

        if (attempt.getStatus() == AttemptStatus.GAME_OVER
                || attempt.getStatus() == AttemptStatus.COMPLETED
                || attempt.getStatus() == AttemptStatus.TIMED_OUT) {
            Map<String, Object> r = new HashMap<>();
            r.put(IS_CORRECT, false);
            r.put(LIVES_REMAINING, attempt.getLivesRemaining());
            r.put(GAME_OVER, attempt.getStatus() == AttemptStatus.GAME_OVER);
            r.put(PERCENTAGE, attempt.getPercentage());
            r.put(XP_EARNED, attempt.getXpEarned());
            return r;
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Attempt is not in progress");
        }

        Optional<StudentAnswer> existingAnswer = studentAnswerRepository
                .findByAttemptAndQuestionId(attempt, questionId);
        if (existingAnswer.isPresent()) {
            StudentAnswer existing = existingAnswer.get();
            Map<String, Object> r = new HashMap<>();
            r.put(IS_CORRECT, existing.getIsCorrect());
            r.put(LIVES_REMAINING, attempt.getLivesRemaining());
            r.put(POINTS_EARNED, existing.getPointsEarned());
            r.put(GAME_OVER, false);
            r.put(XP_EARNED, 0);
            return r;
        }

        Quiz quiz = quizRepository.findById(attempt.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Question question = quiz.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Question not found"));

        boolean isCorrect = question.getAnswers().stream()
                .anyMatch(a -> a.getId().equals(selectedAnswerId) && a.getIsCorrect());

        int pointsEarned = isCorrect ? question.getPoints() : 0;

        StudentAnswer studentAnswer = new StudentAnswer();
        studentAnswer.setAttempt(attempt);
        studentAnswer.setQuestionId(questionId);
        studentAnswer.setSelectedAnswerId(selectedAnswerId);
        studentAnswer.setIsCorrect(isCorrect);
        studentAnswer.setPointsEarned(pointsEarned);
        studentAnswer.setResponseTimeSeconds(responseTimeSec);
        studentAnswerRepository.save(studentAnswer);

        if (isCorrect) {
            attempt.setScore(attempt.getScore() + pointsEarned);
        } else {
            int newLives = attempt.getLivesRemaining() - 1;
            attempt.setLivesRemaining(newLives);

            if (newLives <= 0) {
                attempt.setStatus(AttemptStatus.GAME_OVER);
                attempt.setCompletedAt(LocalDateTime.now());
                calculateFinalScore(attempt);
                attemptRepository.save(attempt);

                Map<String, Object> result = new HashMap<>();
                result.put(IS_CORRECT, false);
                result.put(LIVES_REMAINING, 0);
                result.put(GAME_OVER, true);
                result.put(CORRECT_ANSWER_ID, getCorrectAnswerId(question));
                return result;
            }
        }

        attemptRepository.save(attempt);

        Map<String, Object> result = new HashMap<>();
        result.put(IS_CORRECT, isCorrect);
        result.put(LIVES_REMAINING, attempt.getLivesRemaining());
        result.put(POINTS_EARNED, pointsEarned);
        result.put(GAME_OVER, false);
        result.put(CORRECT_ANSWER_ID, getCorrectAnswerId(question));
        result.put(XP_EARNED, isCorrect ? calculateXpForQuestion(question, responseTimeSec) : 0);
        return result;
    }

    @Transactional
    public QuizAttempt completeQuiz(Long attemptId) {
        QuizAttempt attempt = getAttemptById(attemptId);

        if (attempt.getStatus() == AttemptStatus.GAME_OVER
                || attempt.getStatus() == AttemptStatus.TIMED_OUT
                || attempt.getStatus() == AttemptStatus.COMPLETED) {
            return attempt;
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Attempt is not in progress");
        }

        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setCompletedAt(LocalDateTime.now());
        calculateFinalScore(attempt);
        return attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt timeoutQuiz(Long attemptId) {
        QuizAttempt attempt = getAttemptById(attemptId);
        attempt.setStatus(AttemptStatus.TIMED_OUT);
        attempt.setCompletedAt(LocalDateTime.now());
        calculateFinalScore(attempt);
        return attemptRepository.save(attempt);
    }

    public QuizAttempt getAttemptById(Long id) {
        return attemptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + id));
    }

    public List<QuizAttempt> getByStudent(Long studentId) {
        return attemptRepository.findByStudentId(studentId);
    }

    public List<QuizAttempt> getByQuiz(Long quizId) {
        return attemptRepository.findByQuizId(quizId);
    }

    public List<QuizAttempt> getCompletedByStudent(Long studentId) {
        return attemptRepository.findByStudentIdAndStatus(studentId, AttemptStatus.COMPLETED);
    }

    public Map<String, Object> getStudentStats(Long studentId) {
        int totalXp = attemptRepository.findTotalXp(studentId);
        int level = totalXp / 100;
        int xpToNextLevel = 100 - (totalXp % 100);
        long completed = attemptRepository.countCompleted(studentId);
        long perfectScores = attemptRepository.countPerfectScores(studentId);
        int activeDays = attemptRepository.countActiveDaysLast30(studentId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalXp", totalXp);
        stats.put("level", Math.max(1, level));
        stats.put("xpToNextLevel", xpToNextLevel);
        stats.put("currentStreak", activeDays);
        stats.put("completed", completed);
        stats.put("perfectScores", perfectScores);
        stats.put("badges", calculateBadges(studentId, completed, perfectScores, activeDays));
        return stats;
    }

    public List<Object[]> getMonthlyLeaderboard() {
        return attemptRepository.findMonthlyLeaderboard();
    }

    private void calculateFinalScore(QuizAttempt attempt) {
        if (attempt.getStartedAt() != null && attempt.getCompletedAt() != null) {
            long seconds = ChronoUnit.SECONDS.between(
                    attempt.getStartedAt(), attempt.getCompletedAt());
            attempt.setDurationSeconds((int) seconds);
        }

        if (attempt.getTotalPoints() > 0) {
            double pct = (attempt.getScore() * 100.0) / attempt.getTotalPoints();
            attempt.setPercentage(Math.round(pct * 10.0) / 10.0);
        }

        Quiz quiz = quizRepository.findById(attempt.getQuizId()).orElse(null);
        if (quiz != null) {
            attempt.setIsPassed(attempt.getPercentage() >= quiz.getPassingScore());
        }

        attempt.setXpEarned(calculateXpForAttempt(attempt));
    }

    private int calculateXpForAttempt(QuizAttempt attempt) {
        int baseXp = (int) (attempt.getPercentage() / 10);
        int speedBonus = 0;
        if (attempt.getDurationSeconds() != null) {
            Quiz quiz = quizRepository.findById(attempt.getQuizId()).orElse(null);
            if (quiz != null) {
                int maxSeconds = quiz.getDuration() * 60;
                if (attempt.getDurationSeconds() < maxSeconds * 0.5) speedBonus = 20;
            }
        }
        int perfectBonus = attempt.getPercentage() == 100.0 ? 50 : 0;
        return baseXp + speedBonus + perfectBonus;
    }

    private int calculateXpForQuestion(Question question, Integer responseTimeSec) {
        int base = question.getPoints() * 10;
        int speedBonus = (responseTimeSec != null && responseTimeSec < 5) ? 5 : 0;
        return base + speedBonus;
    }

    private Long getCorrectAnswerId(Question question) {
        return question.getAnswers().stream()
                .filter(Answer::getIsCorrect)
                .map(Answer::getId)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Boolean> calculateBadges(Long studentId, long completed,
                                                 long perfectScores, int streak) {
        Map<String, Boolean> badges = new HashMap<>();
        badges.put("firstStep", completed >= 1);
        badges.put("risingstar", perfectScores >= 1);
        badges.put("weekWarrior", streak >= 7);
        badges.put("perfectionist", perfectScores >= 10);
        badges.put("knowledgeSeeker", completed >= 50);
        return badges;
    }

    public Map<String, Object> getDetailedQuizStats(Long quizId) {
        List<QuizAttempt> attempts = attemptRepository.findByQuizId(quizId);
        List<QuizAttempt> completed = attempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.COMPLETED)
                .toList();

        long passed = completed.stream().filter(QuizAttempt::getIsPassed).count();
        long failed = completed.stream().filter(a -> !a.getIsPassed()).count();
        long gameOver = attempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.GAME_OVER).count();
        double avgScore = completed.stream()
                .mapToDouble(QuizAttempt::getPercentage).average().orElse(0);
        double avgDuration = completed.stream()
                .filter(a -> a.getDurationSeconds() != null)
                .mapToLong(QuizAttempt::getDurationSeconds).average().orElse(0);
        double bestScore = completed.stream()
                .mapToDouble(QuizAttempt::getPercentage).max().orElse(0);
        Map<String, Double> scoreByDay = completed.stream()
                .filter(a -> a.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getCompletedAt().toLocalDate().toString(),
                        Collectors.averagingDouble(QuizAttempt::getPercentage)
                ));
        long totalXp = completed.stream()
                .mapToLong(a -> a.getXpEarned() != null ? a.getXpEarned() : 0).sum();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalAttempts", attempts.size());
        stats.put("completed", completed.size());
        stats.put("passed", passed);
        stats.put("failed", failed);
        stats.put(GAME_OVER, gameOver);
        stats.put("passRate", completed.isEmpty() ? 0 :
                Math.round((passed * 100.0) / completed.size()));
        stats.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
        stats.put("bestScore", Math.round(bestScore * 10.0) / 10.0);
        stats.put("avgDuration", Math.round(avgDuration));
        stats.put("totalXpGiven", totalXp);
        stats.put("scoreByDay", scoreByDay);
        return stats;
    }

    public List<Map<String, Object>> getStudentProgression(Long studentId) {
        List<QuizAttempt> attempts = attemptRepository.findByStudentId(studentId).stream()
                .filter(a -> a.getStatus() == AttemptStatus.COMPLETED)
                .sorted(Comparator.comparing(QuizAttempt::getCompletedAt))
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();
        long cumulativeXp = 0;
        for (QuizAttempt a : attempts) {
            cumulativeXp += a.getXpEarned() != null ? a.getXpEarned() : 0;
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", a.getCompletedAt().toLocalDate().toString());
            point.put("score", a.getPercentage());
            point.put(XP_EARNED, a.getXpEarned());
            point.put("cumulativeXp", cumulativeXp);
            point.put("isPassed", a.getIsPassed());
            point.put("quizId", a.getQuizId());
            result.add(point);
        }
        return result;
    }

    public List<Map<String, Object>> getStudentBadges(Long studentId) {
        List<QuizAttempt> attempts = attemptRepository.findByStudentId(studentId);
        List<QuizAttempt> completed = attempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.COMPLETED)
                .toList();

        long passed = completed.stream().filter(QuizAttempt::getIsPassed).count();
        long perfectScore = completed.stream()
                .filter(a -> a.getPercentage() >= 100).count();
        long totalXp = completed.stream()
                .mapToLong(a -> a.getXpEarned() != null ? a.getXpEarned() : 0).sum();
        long fastAnswers = attempts.stream()
                .filter(a -> a.getDurationSeconds() != null
                        && a.getDurationSeconds() < 60
                        && a.getStatus() == AttemptStatus.COMPLETED).count();

        List<Map<String, Object>> badges = new ArrayList<>();
        if (!completed.isEmpty()) badges.add(badge("first_quiz", "🎯 First Quiz",
                "Completed your first quiz", "gold", true));
        badges.add(badge("quiz_master", "🏆 Quiz Master",
                passed >= 5 ? "Passed 5 quizzes" : passed + "/5 quizzes passed",
                "gold", passed >= 5));
        badges.add(badge("perfect_score", "⭐ Perfect Score",
                perfectScore >= 1 ? "Got 100% on a quiz" : "Get 100% on any quiz",
                "gold", perfectScore >= 1));
        badges.add(badge("xp_500", "⚡ XP Hunter",
                totalXp >= 500 ? "Earned 500 XP" : totalXp + "/500 XP earned",
                "purple", totalXp >= 500));
        badges.add(badge("speed_runner", "🚀 Speed Runner",
                fastAnswers >= 3 ? "Completed 3 quizzes under 1 min" :
                        fastAnswers + "/3 fast completions", "blue", fastAnswers >= 3));
        badges.add(badge("xp_1000", "💎 XP Legend",
                totalXp >= 1000 ? "Earned 1000 XP" : totalXp + "/1000 XP earned",
                "diamond", totalXp >= 1000));
        return badges;
    }

    private Map<String, Object> badge(String id, String name, String description,
                                      String color, boolean unlocked) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("id", id);
        b.put("name", name);
        b.put("description", description);
        b.put("color", color);
        b.put("unlocked", unlocked);
        return b;
    }
}