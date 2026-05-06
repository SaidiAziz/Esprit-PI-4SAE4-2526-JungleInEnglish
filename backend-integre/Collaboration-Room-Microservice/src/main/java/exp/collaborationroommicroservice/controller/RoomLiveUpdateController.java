package exp.collaborationroommicroservice.controller;

import exp.collaborationroommicroservice.exception.BadRequestException;
import exp.collaborationroommicroservice.security.JwtUtil;
import exp.collaborationroommicroservice.service.RoomLiveUpdateService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/collaboration/rooms/{roomId}/events")
@RequiredArgsConstructor
public class RoomLiveUpdateController {

    private final JwtUtil jwtUtil;
    private final RoomService roomService;
    private final RoomLiveUpdateService roomLiveUpdateService;

    @GetMapping("/stream")
    public SseEmitter stream(@PathVariable Long roomId, @RequestParam String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BadRequestException("Invalid token for room live updates");
        }
        roomService.getById(roomId);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return roomLiveUpdateService.subscribe(roomId, userId);
    }
}
