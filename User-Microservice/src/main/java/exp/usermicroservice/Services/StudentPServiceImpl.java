package exp.usermicroservice.Services;

import exp.usermicroservice.Entities.StudentProfile;
import exp.usermicroservice.Repositories.StudentProfileRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class StudentPServiceImpl implements StudentPServiceI{

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Override
    public StudentProfile createStudentProfile(StudentProfile studentProfile) {
        if (studentProfile.getUser() != null && studentProfile.getUser().getId() != null) {
            StudentProfile existingProfile = studentProfileRepository.findByUser_Id(studentProfile.getUser().getId()).orElse(null);
            if (existingProfile != null) {
                existingProfile.setLevel(studentProfile.getLevel());
                existingProfile.setLearningGoals(studentProfile.getLearningGoals());
                return studentProfileRepository.save(existingProfile);
            }
        }
        return studentProfileRepository.save(studentProfile);
    }

    @Override
    public StudentProfile getStudentProfileById(Long id) {
        return studentProfileRepository.findById(id).orElse(null);
    }

    @Override
    public StudentProfile getStudentProfileByUserId(Long userId) {
        return studentProfileRepository.findByUser_Id(userId).orElse(null);
    }

    @Override
    public StudentProfile updateStudentProfile(Long id, StudentProfile studentProfile) {
        return studentProfileRepository.findById(id).map(existingProfile -> {;
            existingProfile.setUser(studentProfile.getUser());
            existingProfile.setLevel(studentProfile.getLevel());
            existingProfile.setLearningGoals(studentProfile.getLearningGoals());
            return studentProfileRepository.save(existingProfile);
        }).orElse(null);
    }

    @Override
    public void deleteStudentProfile(Long id) {
        studentProfileRepository.deleteById(id);
    }

    @Override
    public List<StudentProfile> getAllStudentProfiles() {
        return studentProfileRepository.findAll();
    }
}
