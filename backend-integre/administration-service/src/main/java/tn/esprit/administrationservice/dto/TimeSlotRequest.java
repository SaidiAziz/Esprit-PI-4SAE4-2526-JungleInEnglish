package tn.esprit.administrationservice.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import tn.esprit.administrationservice.entity.OwnerType;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
public class TimeSlotRequest {
    @NotNull
    private OwnerType ownerType;

    private Long ownerId;

    @NotNull
    @Min(1) @Max(7)
    private Integer dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private LocalDate validFrom;
    private LocalDate validTo;

    @NotNull
    private Boolean active = true;
}
