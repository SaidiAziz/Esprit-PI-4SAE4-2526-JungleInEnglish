package exp.collaborationroommicroservice.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RoomLiveUpdateService {

    SseEmitter subscribe(Long roomId, Long userId);

    void publish(Long roomId, String eventType, String message);
}
