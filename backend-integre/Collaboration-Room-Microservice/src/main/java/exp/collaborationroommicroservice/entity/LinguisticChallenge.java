package exp.collaborationroommicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "linguistic_challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinguisticChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long creatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    private String correctAnswer;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeDifficulty difficulty;

    private int pointsReward;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;
}
