package tn.esprit.examen.nomPrenomClasseExamen.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ForumReportTargetType targetType;

    @NotNull
    @Column(nullable = false)
    private Long targetId;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String reporterName;

    @NotBlank
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ForumReportStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;

    @Size(max = 80)
    @Column(length = 80)
    private String reviewedBy;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (reviewedAt == null) reviewedAt = createdAt;
        if (status == null) status = ForumReportStatus.PENDING;
    }
}

