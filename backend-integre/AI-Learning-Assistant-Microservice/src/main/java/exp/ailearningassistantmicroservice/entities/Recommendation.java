package exp.ailearningassistantmicroservice.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    RecommendationType type;

    @Column(nullable = false)
    Long contentId;

    @Column(nullable = false, length = 500)
    String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    SkillType focusSkill;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
