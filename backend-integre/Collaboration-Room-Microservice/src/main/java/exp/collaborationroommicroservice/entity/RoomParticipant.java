package exp.collaborationroommicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_participants", uniqueConstraints = @UniqueConstraint(columnNames = {"roomId", "userId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Column(nullable = false)
    private LocalDateTime lastActiveAt;

    private int messagesCount;
    private int correctionsGiven;
    private int correctionsReceived;
    private double reputationScore;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        joinedAt = now;
        lastActiveAt = now;
    }
}
