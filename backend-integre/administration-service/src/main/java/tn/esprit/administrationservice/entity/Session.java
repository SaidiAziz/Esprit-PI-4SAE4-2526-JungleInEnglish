package tn.esprit.administrationservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "tutorId est obligatoire")
    private Long tutorId;     // référence User/Tutor service

    @NotNull(message = "courseId est obligatoire")
    private Long courseId;    // référence Course service

    @NotNull(message = "sessionDate est obligatoire")
    @FutureOrPresent(message = "sessionDate doit être aujourd'hui ou dans le futur")
    private LocalDate sessionDate;

    @NotNull(message = "startTime est obligatoire")
    private LocalTime startTime;

    @NotNull(message = "endTime est obligatoire")
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private SessionStatus status; // tu peux laisser sans @NotNull si tu le forces dans create()

  @Enumerated(EnumType.STRING)
  private SessionMode mode;

  private String roomName;

  private String meetingLink;

  @Enumerated(EnumType.STRING)
  private MeetingProvider meetingProvider;

    // optionnel
    private Long timeSlotId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ Validation métier : endTime > startTime
    @AssertTrue(message = "endTime doit être strictement après startTime")
    public boolean isTimeRangeValid() {
        if (startTime == null || endTime == null) return true; // laisse @NotNull gérer ça
        return endTime.isAfter(startTime);
    }
}
