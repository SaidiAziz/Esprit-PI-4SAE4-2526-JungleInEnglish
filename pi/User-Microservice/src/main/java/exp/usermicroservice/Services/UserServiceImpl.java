package exp.usermicroservice.Services;

import exp.usermicroservice.DTO.Request.RegisterUserRequest;
import exp.usermicroservice.Entities.Role;
import exp.usermicroservice.Entities.StudentProfile;
import exp.usermicroservice.Entities.TutorProfile;
import exp.usermicroservice.Entities.User;
import exp.usermicroservice.Repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserServiceI{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentPServiceI studentPService;

    @Autowired
    private TutorPServiceI tutorPService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(User user, RegisterUserRequest request) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User savedUser = userRepository.save(user);
        createProfileForRole(savedUser, request);
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());
        return savedUser;
    }

    private void createProfileForRole(User user, RegisterUserRequest request) {
        if (user.getRole() == Role.STUDENT) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(user);
            if (request != null) {
                profile.setLevel(request.getLevel());
                profile.setLearningGoals(request.getLearningGoals());
            }
            studentPService.createStudentProfile(profile);
            log.info("Created StudentProfile for user id={}", user.getId());
        } else if (user.getRole() == Role.TUTOR) {
            TutorProfile profile = new TutorProfile();
            profile.setUser(user);
            if (request != null) {
                profile.setBio(request.getBio());
                profile.setSpecialization(request.getSpecialization());
                profile.setExperienceYears(request.getExperienceYears());
                profile.setHourlyRate(request.getHourlyRate());
            }
            tutorPService.createTutorProfile(profile);
            log.info("Created TutorProfile for user id={}", user.getId());
        }
        // ADMIN has no profile
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User updateUser(Long id, User user) {
        return userRepository.findById(id).map(existingUser -> {;
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPassword(user.getPassword());
            return userRepository.save(existingUser);
        }).orElse(null);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
