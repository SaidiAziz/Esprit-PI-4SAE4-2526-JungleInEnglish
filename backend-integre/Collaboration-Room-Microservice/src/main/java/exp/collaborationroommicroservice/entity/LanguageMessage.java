package exp.collaborationroommicroservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "language_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private boolean translationRequest;

    @Column(columnDefinition = "TEXT")
    private String autoTranslation;

    @Column(nullable = false)
    private boolean hasCorrectionRequest;

    private String mediaUrl;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @PrePersist
    void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
