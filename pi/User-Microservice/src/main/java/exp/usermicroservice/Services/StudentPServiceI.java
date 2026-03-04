package exp.usermicroservice.Services;

import exp.usermicroservice.Entities.StudentProfile;

import java.util.List;

public interface StudentPServiceI {
    StudentProfile createStudentProfile(StudentProfile studentProfile);
    StudentProfile getStudentProfileById(Long id);
    StudentProfile getStudentProfileByUserId(Long userId);
    StudentProfile updateStudentProfile(Long id, StudentProfile studentProfile);
    void deleteStudentProfile(Long id);
    List<StudentProfile> getAllStudentProfiles();
}
