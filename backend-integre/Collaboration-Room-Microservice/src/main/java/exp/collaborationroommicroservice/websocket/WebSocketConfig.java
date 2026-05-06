package exp.collaborationroommicroservice.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final RoomSignalWebSocketHandler roomSignalWebSocketHandler;
    private final RoomSignalHandshakeInterceptor roomSignalHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(roomSignalWebSocketHandler, "/api/collaboration/ws/call")
                .addInterceptors(roomSignalHandshakeInterceptor)
                .setAllowedOrigins("http://localhost:4200");
    }
}
