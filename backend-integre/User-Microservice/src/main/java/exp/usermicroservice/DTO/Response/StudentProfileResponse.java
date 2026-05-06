package exp.usermicroservice.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {
    private Long id;
    private String level;
    private String learningGoals;
    private UserResponse user;
}
