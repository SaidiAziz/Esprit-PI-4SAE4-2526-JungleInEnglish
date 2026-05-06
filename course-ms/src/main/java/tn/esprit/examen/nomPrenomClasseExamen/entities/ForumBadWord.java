package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
        name = "forum_bad_words",
        uniqueConstraints = @UniqueConstraint(name = "uk_forum_bad_word", columnNames = {"word"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumBadWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String word;
}

