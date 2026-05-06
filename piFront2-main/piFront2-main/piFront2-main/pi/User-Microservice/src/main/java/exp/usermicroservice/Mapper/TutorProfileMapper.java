package exp.usermicroservice.Mapper;

import exp.usermicroservice.DTO.Request.CreateTutorProfileRequest;
import exp.usermicroservice.DTO.Request.UpdateTutorProfileRequest;
import exp.usermicroservice.DTO.Response.TutorProfileResponse;
import exp.usermicroservice.Entities.TutorProfile;
import exp.usermicroservice.Entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TutorProfileMapper {

    private final UserMapper userMapper;

    public TutorProfile toEntity(CreateTutorProfileRequest request, User user) {
        if (request == null) return null;
        TutorProfile profile = new TutorProfile();
        profile.setUser(user);
        profile.setBio(request.getBio());
        profile.setSpecialization(request.getSpecialization());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setHourlyRate(request.getHourlyRate());
        return profile;
    }

    public void updateEntity(TutorProfile existing, UpdateTutorProfileRequest request) {
        if (existing == null || request == null) return;
        if (request.getBio() != null) existing.setBio(request.getBio());
        if (request.getSpecialization() != null) existing.setSpecialization(request.getSpecialization());
        if (request.getExperienceYears() != null) existing.setExperienceYears(request.getExperienceYears());
        if (request.getHourlyRate() != null) existing.setHourlyRate(request.getHourlyRate());
    }

    public TutorProfileResponse toResponse(TutorProfile profile) {
        if (profile == null) return null;
        return TutorProfileResponse.builder()
                .id(profile.getUserId())
                .bio(profile.getBio())
                .specialization(profile.getSpecialization())
                .experienceYears(profile.getExperienceYears())
                .hourlyRate(profile.getHourlyRate())
                .user(userMapper.toResponse(profile.getUser()))
                .build();
    }
}
