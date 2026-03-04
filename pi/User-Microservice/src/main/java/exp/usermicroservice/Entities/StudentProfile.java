package exp.usermicroservice.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StudentProfile {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long userId;

    private String level;
    private String LearningGoals;

    //private String subscriptionPlan;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
