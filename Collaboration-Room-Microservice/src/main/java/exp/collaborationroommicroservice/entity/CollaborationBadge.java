package exp.collaborationroommicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "collaboration_badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborationBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeType badgeType;

    private Long roomId;

    @Column(nullable = false)
    private LocalDateTime earnedAt;

    @PrePersist
    void onCreate() {
        earnedAt = LocalDateTime.now();
    }
}
