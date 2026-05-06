package exp.usermicroservice.Controllers;

import exp.usermicroservice.DTO.Request.CreateStudentProfileRequest;
import exp.usermicroservice.DTO.Request.UpdateStudentProfileRequest;
import exp.usermicroservice.DTO.Response.StudentProfileResponse;
import exp.usermicroservice.Entities.StudentProfile;
import exp.usermicroservice.Entities.User;
import exp.usermicroservice.Mapper.StudentProfileMapper;
import exp.usermicroservice.Security.SecurityUtils;
import exp.usermicroservice.Services.StudentPServiceI;
import exp.usermicroservice.Services.UserServiceI;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/studentProfile")
@AllArgsConstructor
public class StudentProfileController {

    private final UserServiceI userService;
    private final StudentPServiceI studentPService;
    private final StudentProfileMapper studentProfileMapper;

    @PostMapping("/createStudentProfile")
    public ResponseEntity<StudentProfileResponse> createStudentProfile(@RequestBody CreateStudentProfileRequest request) {
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        StudentProfile profile = studentProfileMapper.toEntity(request, user);
        StudentProfile created = studentPService.createStudentProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(studentProfileMapper.toResponse(created));
    }

    @PutMapping("/updateStudentProfile/{id}")
    public ResponseEntity<StudentProfileResponse> updateStudentProfile(
            @PathVariable("id") Long id,
            @RequestBody UpdateStudentProfileRequest request) {
        StudentProfile existing = studentPService.getStudentProfileById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        studentProfileMapper.updateEntity(existing, request);
        StudentProfile updated = studentPService.updateStudentProfile(id, existing);
        return ResponseEntity.ok(studentProfileMapper.toResponse(updated));
    }

    @DeleteMapping("/deleteStudentProfile/{id}")
    public ResponseEntity<Void> deleteStudentProfile(@PathVariable("id") Long id) {
        if (studentPService.getStudentProfileById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        studentPService.deleteStudentProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getStudentProfileById/{id}")
    public ResponseEntity<StudentProfileResponse> getStudentProfileById(@PathVariable("id") Long id) {
        StudentProfile profile = studentPService.getStudentProfileById(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(studentProfileMapper.toResponse(profile));
    }

    @GetMapping("/getAllStudentProfiles")
    public List<StudentProfileResponse> getAllStudentProfiles() {
        return studentPService.getAllStudentProfiles().stream()
                .map(studentProfileMapper::toResponse)
                .toList();
    }

    @GetMapping("/me")
    public ResponseEntity<StudentProfileResponse> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        StudentProfile profile = studentPService.getStudentProfileByUserId(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(studentProfileMapper.toResponse(profile));
    }
}
