package exp.ailearningassistantmicroservice.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "learning_paths",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "courseId"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;

    @Column(nullable = false)
    Long courseId;

    @Column(nullable = false, columnDefinition = "TEXT")
    String lessonOrder;

    double progress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    SkillType focusSkill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    StudentLevel targetLevel;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
