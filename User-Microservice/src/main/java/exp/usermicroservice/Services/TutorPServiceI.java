package exp.usermicroservice.Services;

import exp.usermicroservice.Entities.TutorProfile;

import java.util.List;

public interface TutorPServiceI {
    TutorProfile createTutorProfile(TutorProfile tutorProfile);
    TutorProfile getTutorProfileById(Long id);
    TutorProfile getTutorProfileByUserId(Long userId);
    TutorProfile updateTutorProfile(Long id, TutorProfile tutorProfile);
    void deleteTutorProfile(Long id);
    List<TutorProfile> getAllTutorProfiles();
}
