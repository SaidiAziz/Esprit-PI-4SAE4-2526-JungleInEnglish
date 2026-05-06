package exp.collaborationroommicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "peer_corrections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeerCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long messageId;

    @Column(nullable = false)
    private Long correctorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String correctedText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CorrectionErrorType errorType;

    @Column(nullable = false)
    private boolean accepted;

    private int helpfulVotes;

    @Column(nullable = false)
    private LocalDateTime correctedAt;

    @PrePersist
    void onCreate() {
        correctedAt = LocalDateTime.now();
    }
}
