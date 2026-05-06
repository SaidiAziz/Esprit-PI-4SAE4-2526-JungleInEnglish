package exp.collaborationroommicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "collaboration_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborationRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String nativeLanguage;

    @Column(nullable = false)
    private String targetLanguage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private boolean isPublic;

    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status;

    private Long courseId;

    @Column(nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
