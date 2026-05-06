package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
        name = "forum_user_stats",
        uniqueConstraints = @UniqueConstraint(name = "uk_forum_user_stats_author", columnNames = {"author_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumUserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "author_name", nullable = false, length = 80)
    private String authorName;

    @NotNull
    @Column(nullable = false)
    private Long points;

    @NotNull
    @Column(nullable = false)
    private Long postsCount;

    @NotNull
    @Column(nullable = false)
    private Long commentsCount;

    @PrePersist
    void prePersist() {
        if (points == null) points = 0L;
        if (postsCount == null) postsCount = 0L;
        if (commentsCount == null) commentsCount = 0L;
    }
}

