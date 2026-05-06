package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.dto.ChangeParticipantRoleRequest;
import exp.collaborationroommicroservice.dto.response.ParticipantResponse;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.security.SecurityUtils;
import exp.collaborationroommicroservice.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaboration/rooms/{roomId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;
    private final CollaborationMapper collaborationMapper;

    @PostMapping("/join")
    public ParticipantResponse join(@PathVariable Long roomId) {
        return collaborationMapper.toResponse(participantService.join(roomId, SecurityUtils.getCurrentUserId()));
    }

    @DeleteMapping("/leave")
    public ResponseEntity<Void> leave(@PathVariable Long roomId) {
        participantService.leave(roomId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<ParticipantResponse> getParticipants(@PathVariable Long roomId) {
        return participantService.getRoomParticipants(roomId).stream().map(collaborationMapper::toResponse).toList();
    }

    @GetMapping("/{userId}")
    public ParticipantResponse getParticipant(@PathVariable Long roomId, @PathVariable Long userId) {
        return collaborationMapper.toResponse(participantService.getParticipant(roomId, userId));
    }

    @PatchMapping("/{userId}/role")
    public ParticipantResponse changeRole(@PathVariable Long roomId,
                                          @PathVariable Long userId,
                                          @Valid @RequestBody ChangeParticipantRoleRequest request) {
        return collaborationMapper.toResponse(
                participantService.changeRole(roomId, userId, SecurityUtils.getCurrentUserId(), request.getRole())
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> kick(@PathVariable Long roomId, @PathVariable Long userId) {
        participantService.kick(roomId, userId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
