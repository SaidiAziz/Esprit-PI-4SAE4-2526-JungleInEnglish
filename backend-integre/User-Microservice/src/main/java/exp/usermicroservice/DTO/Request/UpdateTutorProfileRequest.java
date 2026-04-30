package exp.usermicroservice.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTutorProfileRequest {
    private String bio;
    private String specialization;
    private Integer experienceYears;
    private Float hourlyRate;
}
