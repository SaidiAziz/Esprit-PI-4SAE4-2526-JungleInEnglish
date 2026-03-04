package exp.usermicroservice.Services;

import exp.usermicroservice.Entities.TutorProfile;
import exp.usermicroservice.Repositories.TutorProfileRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TutorPServiceImpl implements TutorPServiceI{

    @Autowired
    private TutorProfileRepository tutorProfileRepository;

    @Override
    public TutorProfile createTutorProfile(TutorProfile tutorProfile) {
        return tutorProfileRepository.save(tutorProfile);
    }

    @Override
    public TutorProfile getTutorProfileById(Long id) {
        return tutorProfileRepository.findById(id).orElse(null);
    }

    @Override
    public TutorProfile getTutorProfileByUserId(Long userId) {
        return tutorProfileRepository.findByUser_Id(userId).orElse(null);
    }

    @Override
    public TutorProfile updateTutorProfile(Long id, TutorProfile tutorProfile) {
        return tutorProfileRepository.findById(id).map(existingProfile -> {;
            existingProfile.setUser(tutorProfile.getUser());
            existingProfile.setBio(tutorProfile.getBio());
            existingProfile.setSpecialization(tutorProfile.getSpecialization());
            existingProfile.setHourlyRate(tutorProfile.getHourlyRate());
            existingProfile.setExperienceYears(tutorProfile.getExperienceYears());
            return tutorProfileRepository.save(existingProfile);
        }).orElse(null);
    }

    @Override
    public void deleteTutorProfile(Long id) {
        tutorProfileRepository.deleteById(id);
    }

    @Override
    public List<TutorProfile> getAllTutorProfiles() {
        return tutorProfileRepository.findAll();
    }
}
