package exp.collaborationroommicroservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import exp.collaborationroommicroservice.entity.RoomType;
import exp.collaborationroommicroservice.exception.BadRequestException;
import exp.collaborationroommicroservice.repository.RoomParticipantRepository;
import exp.collaborationroommicroservice.service.ExchangeImpactService;
import exp.collaborationroommicroservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RoomSignalWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RoomService roomService;
    private final RoomParticipantRepository participantRepository;
    private final ExchangeImpactService exchangeImpactService;

    private final Map<Long, Map<Long, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        RoomSignalMessage signal = objectMapper.readValue(message.getPayload(), RoomSignalMessage.class);
        Long userId = currentUserId(session);

        switch (signal.getType()) {
            case "join-call" -> joinCall(session, userId, signal.getRoomId());
            case "leave-call" -> leaveCall(session, userId);
            case "offer", "answer", "ice-candidate" -> relaySignal(session, userId, signal);
            case "hand-state" -> broadcastSignal(session, userId, signal);
            default -> sendError(session, "Unsupported signal type");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        leaveCall(session, currentUserId(session));
        super.afterConnectionClosed(session, status);
    }

    private void joinCall(WebSocketSession session, Long userId, Long roomId) throws IOException {
        validateVoiceRoom(roomId, userId);
        roomSessions.computeIfAbsent(roomId, ignored -> new ConcurrentHashMap<>());
        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        List<Long> existingParticipants = new ArrayList<>(sessions.keySet());
        sessions.put(userId, session);
        session.getAttributes().put("roomId", roomId);
        exchangeImpactService.onCallJoined(roomId, userId);

        RoomSignalMessage joined = new RoomSignalMessage();
        joined.setType("joined-call");
        joined.setRoomId(roomId);
        joined.setSenderUserId(userId);
        joined.setParticipantIds(existingParticipants);
        send(session, joined);

        RoomSignalMessage participantJoined = new RoomSignalMessage();
        participantJoined.setType("participant-joined");
        participantJoined.setRoomId(roomId);
        participantJoined.setSenderUserId(userId);
        broadcast(roomId, participantJoined, userId);
    }

    private void leaveCall(WebSocketSession session, Long userId) {
        Long roomId = currentRoomId(session);
        if (roomId == null) {
            return;
        }

        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) {
            return;
        }

        sessions.remove(userId);
        session.getAttributes().remove("roomId");
        exchangeImpactService.onCallLeft(roomId, userId);
        RoomSignalMessage left = new RoomSignalMessage();
        left.setType("participant-left");
        left.setRoomId(roomId);
        left.setSenderUserId(userId);
        broadcast(roomId, left, userId);

        if (sessions.isEmpty()) {
            roomSessions.remove(roomId);
        }
    }

    private void relaySignal(WebSocketSession session, Long userId, RoomSignalMessage signal) throws IOException {
        Long roomId = currentRoomId(session);
        if (roomId == null || signal.getTargetUserId() == null) {
            sendError(session, "Signal target or room is missing");
            return;
        }

        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) {
            sendError(session, "Call room is not active");
            return;
        }

        WebSocketSession targetSession = sessions.get(signal.getTargetUserId());
        if (targetSession == null || !targetSession.isOpen()) {
            sendError(session, "Target participant is offline");
            return;
        }

        signal.setSenderUserId(userId);
        signal.setRoomId(roomId);
        send(targetSession, signal);
    }

    private void broadcastSignal(WebSocketSession session, Long userId, RoomSignalMessage signal) {
        Long roomId = currentRoomId(session);
        if (roomId == null) {
            return;
        }
        signal.setSenderUserId(userId);
        signal.setRoomId(roomId);
        broadcast(roomId, signal, userId);
    }

    private void validateVoiceRoom(Long roomId, Long userId) {
        var room = roomService.getById(roomId);
        if (room.getType() == RoomType.TEXT_CHAT) {
            throw new BadRequestException("Video call is only available in voice or mixed rooms");
        }
        participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BadRequestException("Join the room before starting a call"));
    }

    private void broadcast(Long roomId, RoomSignalMessage signal, Long excludedUserId) {
        Map<Long, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) {
            return;
        }

        sessions.forEach((participantId, participantSession) -> {
            if (participantId.equals(excludedUserId) || !participantSession.isOpen()) {
                return;
            }
            try {
                send(participantSession, signal);
            } catch (IOException ignored) {
            }
        });
    }

    private void send(WebSocketSession session, RoomSignalMessage signal) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(signal)));
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        RoomSignalMessage error = new RoomSignalMessage();
        error.setType("error");
        error.setPayload(Map.of("message", message));
        send(session, error);
    }

    private Long currentUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId instanceof Long value ? value : null;
    }

    private Long currentRoomId(WebSocketSession session) {
        Object roomId = session.getAttributes().get("roomId");
        return roomId instanceof Long value ? value : null;
    }
}
