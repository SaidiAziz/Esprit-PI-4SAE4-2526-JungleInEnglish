package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.dto.CreateRoomRequest;
import exp.collaborationroommicroservice.dto.UpdateRoomRequest;
import exp.collaborationroommicroservice.dto.UpdateRoomStatusRequest;
import exp.collaborationroommicroservice.dto.response.RoomResponse;
import exp.collaborationroommicroservice.entity.RoomLevel;
import exp.collaborationroommicroservice.mapper.CollaborationMapper;
import exp.collaborationroommicroservice.security.SecurityUtils;
import exp.collaborationroommicroservice.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaboration/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final CollaborationMapper collaborationMapper;

    @PostMapping
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaborationMapper.toResponse(roomService.create(request, SecurityUtils.getCurrentUserId())));
    }

    @GetMapping
    public List<RoomResponse> getPublicRooms() {
        return roomService.getPublicRooms().stream().map(collaborationMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public RoomResponse getById(@PathVariable Long id) {
        return collaborationMapper.toResponse(roomService.getById(id));
    }

    @GetMapping("/my")
    public List<RoomResponse> getMyRooms() {
        return roomService.getMyRooms(SecurityUtils.getCurrentUserId()).stream().map(collaborationMapper::toResponse).toList();
    }

    @GetMapping("/language/{language}")
    public List<RoomResponse> getByLanguage(@PathVariable String language) {
        return roomService.getByLanguage(language).stream().map(collaborationMapper::toResponse).toList();
    }

    @GetMapping("/level/{level}")
    public List<RoomResponse> getByLevel(@PathVariable RoomLevel level) {
        return roomService.getByLevel(level).stream().map(collaborationMapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRoomRequest request) {
        return collaborationMapper.toResponse(roomService.update(id, SecurityUtils.getCurrentUserId(), request));
    }

    @PatchMapping("/{id}/status")
    public RoomResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateRoomStatusRequest request) {
        return collaborationMapper.toResponse(roomService.updateStatus(id, SecurityUtils.getCurrentUserId(), request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roomService.delete(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
