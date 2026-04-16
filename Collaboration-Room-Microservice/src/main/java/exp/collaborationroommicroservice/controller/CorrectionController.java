package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.dto.SubmitCorrectionRequest;
import exp.collaborationroommicroservice.dto.response.CorrectionResponse;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.security.SecurityUtils;
import exp.collaborationroommicroservice.service.CorrectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CorrectionController {

    private final CorrectionService correctionService;
    private final CollaborationMapper collaborationMapper;

    @PostMapping("/api/collaboration/messages/{messageId}/corrections")
    public ResponseEntity<CorrectionResponse> submit(@PathVariable Long messageId, @Valid @RequestBody SubmitCorrectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborationMapper.toResponse(correctionService.submit(messageId, SecurityUtils.getCurrentUserId(), request)));
    }

    @GetMapping("/api/collaboration/messages/{messageId}/corrections")
    public List<CorrectionResponse> getCorrections(@PathVariable Long messageId) {
        return correctionService.getCorrections(messageId).stream().map(collaborationMapper::toResponse).toList();
    }

    @PatchMapping("/api/collaboration/corrections/{id}/accept")
    public CorrectionResponse accept(@PathVariable Long id) {
        return collaborationMapper.toResponse(correctionService.accept(id, SecurityUtils.getCurrentUserId()));
    }

    @PatchMapping("/api/collaboration/corrections/{id}/refuse")
    public CorrectionResponse refuse(@PathVariable Long id) {
        return collaborationMapper.toResponse(correctionService.refuse(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/api/collaboration/corrections/{id}/vote")
    public CorrectionResponse voteHelpful(@PathVariable Long id) {
        return collaborationMapper.toResponse(correctionService.voteHelpful(id));
    }

    @DeleteMapping("/api/collaboration/corrections/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        correctionService.delete(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
