package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.dto.SendMessageRequest;
import exp.collaborationroommicroservice.dto.UpdateMessageFlagsRequest;
import exp.collaborationroommicroservice.dto.response.MessageResponse;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.security.SecurityUtils;
import exp.collaborationroommicroservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaboration/rooms/{roomId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final CollaborationMapper collaborationMapper;

    @PostMapping
    public ResponseEntity<MessageResponse> send(@PathVariable Long roomId, @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborationMapper.toResponse(messageService.send(roomId, SecurityUtils.getCurrentUserId(), request)));
    }

    @GetMapping
    public List<MessageResponse> getMessages(@PathVariable Long roomId) {
        return messageService.getRoomMessages(roomId).stream().map(collaborationMapper::toResponse).toList();
    }

    @GetMapping("/{messageId}")
    public MessageResponse getById(@PathVariable Long roomId, @PathVariable Long messageId) {
        return collaborationMapper.toResponse(messageService.getById(roomId, messageId));
    }

    @PatchMapping("/{messageId}/translation")
    public MessageResponse requestTranslation(@PathVariable Long roomId,
                                              @PathVariable Long messageId) {
        UpdateMessageFlagsRequest request = new UpdateMessageFlagsRequest();
        request.setTranslationRequest(true);
        request.setCorrectionRequest(messageService.getById(roomId, messageId).isHasCorrectionRequest());
        return collaborationMapper.toResponse(messageService.updateFlags(messageId, SecurityUtils.getCurrentUserId(), request));
    }

    @PatchMapping("/{messageId}/correction-request")
    public MessageResponse requestCorrection(@PathVariable Long roomId,
                                             @PathVariable Long messageId) {
        UpdateMessageFlagsRequest request = new UpdateMessageFlagsRequest();
        request.setTranslationRequest(messageService.getById(roomId, messageId).isTranslationRequest());
        request.setCorrectionRequest(true);
        return collaborationMapper.toResponse(messageService.updateFlags(messageId, SecurityUtils.getCurrentUserId(), request));
    }

    @PatchMapping("/{messageId}/translation-seen")
    public MessageResponse acceptTranslation(@PathVariable Long roomId,
                                             @PathVariable Long messageId) {
        return collaborationMapper.toResponse(messageService.acceptTranslation(roomId, messageId, SecurityUtils.getCurrentUserId()));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable Long roomId, @PathVariable Long messageId) {
        messageService.delete(roomId, messageId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
