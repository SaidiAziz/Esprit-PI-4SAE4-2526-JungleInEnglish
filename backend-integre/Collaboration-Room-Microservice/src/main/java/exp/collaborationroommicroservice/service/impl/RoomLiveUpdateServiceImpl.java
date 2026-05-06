package exp.collaborationroommicroservice.service.impl;

import exp.collaborationroommicroservice.service.RoomLiveUpdateService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RoomLiveUpdateServiceImpl implements RoomLiveUpdateService {

    private final Map<Long, List<SseEmitter>> roomEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(Long roomId, Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        roomEmitters.computeIfAbsent(roomId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(roomId, emitter));
        emitter.onTimeout(() -> removeEmitter(roomId, emitter));
        emitter.onError(ignored -> removeEmitter(roomId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Live updates connected for user " + userId));
        } catch (IOException exception) {
            removeEmitter(roomId, emitter);
        }
        return emitter;
    }

    @Override
    public void publish(Long roomId, String eventType, String message) {
        List<SseEmitter> emitters = roomEmitters.get(roomId);
        if (emitters == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(message));
            } catch (IOException exception) {
                removeEmitter(roomId, emitter);
            }
        }
    }

    private void removeEmitter(Long roomId, SseEmitter emitter) {
        List<SseEmitter> emitters = roomEmitters.get(roomId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            roomEmitters.remove(roomId);
        }
    }
}
