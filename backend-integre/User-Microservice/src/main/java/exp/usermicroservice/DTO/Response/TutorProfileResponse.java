package exp.usermicroservice.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorProfileResponse {
    private Long id;
    private String bio;
    private String specialization;
    private Integer experienceYears;
    private Float hourlyRate;
    private UserResponse user;
}
