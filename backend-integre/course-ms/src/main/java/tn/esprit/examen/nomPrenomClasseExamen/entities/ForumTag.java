package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
        name = "forum_tags",
        uniqueConstraints = @UniqueConstraint(name = "uk_forum_tag_slug", columnNames = {"slug"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String slug;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String label;
}

