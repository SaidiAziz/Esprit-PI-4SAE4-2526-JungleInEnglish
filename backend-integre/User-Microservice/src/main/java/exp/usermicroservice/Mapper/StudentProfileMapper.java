package exp.usermicroservice.Mapper;

import exp.usermicroservice.DTO.Request.CreateStudentProfileRequest;
import exp.usermicroservice.DTO.Request.UpdateStudentProfileRequest;
import exp.usermicroservice.DTO.Response.StudentProfileResponse;
import exp.usermicroservice.Entities.StudentProfile;
import exp.usermicroservice.Entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentProfileMapper {

    private final UserMapper userMapper;

    public StudentProfile toEntity(CreateStudentProfileRequest request, User user) {
        if (request == null) return null;
        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        profile.setLevel(request.getLevel());
        profile.setLearningGoals(request.getLearningGoals());
        return profile;
    }

    public void updateEntity(StudentProfile existing, UpdateStudentProfileRequest request) {
        if (existing == null || request == null) return;
        if (request.getLevel() != null) existing.setLevel(request.getLevel());
        if (request.getLearningGoals() != null) existing.setLearningGoals(request.getLearningGoals());
    }

    public StudentProfileResponse toResponse(StudentProfile profile) {
        if (profile == null) return null;
        return StudentProfileResponse.builder()
                .id(profile.getUserId())
                .level(profile.getLevel())
                .learningGoals(profile.getLearningGoals())
                .user(userMapper.toResponse(profile.getUser()))
                .build();
    }
}
