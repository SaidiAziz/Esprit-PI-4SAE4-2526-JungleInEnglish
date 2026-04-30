package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.dto.ChallengeAnswerRequest;
import exp.collaborationroommicroservice.dto.ChallengeAnswerResponse;
import exp.collaborationroommicroservice.dto.CreateChallengeRequest;
import exp.collaborationroommicroservice.dto.UpdateChallengeStatusRequest;
import exp.collaborationroommicroservice.dto.response.ChallengeResponse;
import exp.collaborationroommicroservice.dto.response.ChallengeSubmissionResponse;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.security.SecurityUtils;
import exp.collaborationroommicroservice.service.ChallengeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaboration")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final CollaborationMapper collaborationMapper;

    @PostMapping("/rooms/{roomId}/challenges")
    public ResponseEntity<ChallengeResponse> create(@PathVariable Long roomId, @Valid @RequestBody CreateChallengeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborationMapper.toResponse(challengeService.create(roomId, SecurityUtils.getCurrentUserId(), request)));
    }

    @GetMapping("/rooms/{roomId}/challenges")
    public List<ChallengeResponse> getChallenges(@PathVariable Long roomId) {
        return challengeService.getRoomChallenges(roomId).stream().map(collaborationMapper::toResponse).toList();
    }

    @GetMapping("/challenges/{id}")
    public ChallengeResponse getById(@PathVariable Long id) {
        return collaborationMapper.toResponse(challengeService.getById(id));
    }

    @GetMapping("/challenges/{id}/submissions")
    public List<ChallengeSubmissionResponse> getSubmissions(@PathVariable Long id) {
        return challengeService.getSubmissions(id, SecurityUtils.getCurrentUserId()).stream()
                .map(collaborationMapper::toResponse)
                .toList();
    }

    @PostMapping("/challenges/{id}/submit")
    public ChallengeAnswerResponse submit(@PathVariable Long id, @Valid @RequestBody ChallengeAnswerRequest request) {
        return challengeService.submitAnswer(id, SecurityUtils.getCurrentUserId(), request);
    }

    @PatchMapping("/challenges/{id}/status")
    public ChallengeResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateChallengeStatusRequest request) {
        return collaborationMapper.toResponse(challengeService.updateStatus(id, SecurityUtils.getCurrentUserId(), request.getStatus()));
    }

    @DeleteMapping("/challenges/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        challengeService.delete(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
