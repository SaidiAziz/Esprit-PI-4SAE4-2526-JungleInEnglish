package exp.collaborationroommicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_submissions", uniqueConstraints = @UniqueConstraint(columnNames = {"challengeId", "userId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    private int pointsAwarded;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
