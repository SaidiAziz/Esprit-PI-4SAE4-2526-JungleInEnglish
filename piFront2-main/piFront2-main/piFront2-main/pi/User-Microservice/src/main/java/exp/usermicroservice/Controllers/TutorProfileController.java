package exp.usermicroservice.Controllers;

import exp.usermicroservice.DTO.Request.CreateTutorProfileRequest;
import exp.usermicroservice.DTO.Request.UpdateTutorProfileRequest;
import exp.usermicroservice.DTO.Response.TutorProfileResponse;
import exp.usermicroservice.Entities.TutorProfile;
import exp.usermicroservice.Entities.User;
import exp.usermicroservice.Mapper.TutorProfileMapper;
import exp.usermicroservice.Security.SecurityUtils;
import exp.usermicroservice.Services.TutorPServiceI;
import exp.usermicroservice.Services.UserServiceI;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tutorProfile")
@AllArgsConstructor
public class TutorProfileController {

    private final UserServiceI userService;
    private final TutorPServiceI tutorPService;
    private final TutorProfileMapper tutorProfileMapper;

    @PostMapping("/createTutorProfile")
    public ResponseEntity<TutorProfileResponse> createTutorProfile(@RequestBody CreateTutorProfileRequest request) {
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        TutorProfile profile = tutorProfileMapper.toEntity(request, user);
        TutorProfile created = tutorPService.createTutorProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorProfileMapper.toResponse(created));
    }

    @PutMapping("/updateTutorProfile/{id}")
    public ResponseEntity<TutorProfileResponse> updateTutorProfile(
            @PathVariable("id") Long id,
            @RequestBody UpdateTutorProfileRequest request) {
        TutorProfile existing = tutorPService.getTutorProfileById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        tutorProfileMapper.updateEntity(existing, request);
        TutorProfile updated = tutorPService.updateTutorProfile(id, existing);
        return ResponseEntity.ok(tutorProfileMapper.toResponse(updated));
    }

    @DeleteMapping("/deleteTutorProfile/{id}")
    public ResponseEntity<Void> deleteTutorProfile(@PathVariable("id") Long id) {
        if (tutorPService.getTutorProfileById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        tutorPService.deleteTutorProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getTutorProfileById/{id}")
    public ResponseEntity<TutorProfileResponse> getTutorProfileById(@PathVariable("id") Long id) {
        TutorProfile profile = tutorPService.getTutorProfileById(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tutorProfileMapper.toResponse(profile));
    }

    @GetMapping("/getAllTutorProfiles")
    public List<TutorProfileResponse> getAllTutorProfiles() {
        return tutorPService.getAllTutorProfiles().stream()
                .map(tutorProfileMapper::toResponse)
                .toList();
    }

    @GetMapping("/me")
    public ResponseEntity<TutorProfileResponse> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TutorProfile profile = tutorPService.getTutorProfileByUserId(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tutorProfileMapper.toResponse(profile));
    }
}
