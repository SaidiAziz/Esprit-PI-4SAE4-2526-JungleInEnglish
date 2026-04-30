package exp.ailearningassistantmicroservice.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "performance_analyses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "courseId"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PerformanceAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;

    @Column(nullable = false)
    Long courseId;

    int grammarScore;

    int listeningScore;

    int speakingScore;

    double averageScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    StudentLevel level;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime lastUpdated;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        lastUpdated = now;
    }

    @PreUpdate
    void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
